package persistence;

import entities.*;
import logic.Criterion;
import logic.Searchable;
import org.joda.time.DateTime;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static logic.CriterionOperator.EqualTo;

/**
 * @author Marcello De Bernardi
 */
class ObjectRelationalMapper {
    // singleton instance
    private static ObjectRelationalMapper instance;

    // helper classes
    private ForeignKeyResolver resolver;

    // reflection data
    private List<Class<? extends Searchable>> searchableClasses;
    private HashMap<Class<? extends Searchable>, Constructor<?>> constructors;
    private HashMap<Class<? extends Searchable>, List<Method>> getterLists;
    private HashMap<Class<?>, Method> primaryGetters;
    private List<Class<?>> numericDataTypes;

    // database structure
    private HashMap<Class<? extends Searchable>, Class<? extends Searchable>> dependencyMap;


    // todo get reflection data once


    private ObjectRelationalMapper() {
        resolver = ForeignKeyResolver.getInstance();
    }

    /**
     * Returns a reference to the singleton instance of this class.
     *
     * @return singleton instance.
     */
    static ObjectRelationalMapper getInstance() {
        if (instance == null) instance = new ObjectRelationalMapper();
        return instance;
    }

    /**
     * Converts criterion to SQL SELECT query
     */
    <E extends Searchable> String toSELECTQuery(Criterion<E> criteria) {
        return "SELECT * FROM " + criteria.getCriterionClass().getSimpleName()
                + (criteria.toString().equals("") ? "" : " WHERE ")
                + criteria.toString() + ";";
    }

    /**
     * Converts an entity into an insertion transaction
     */
    <E extends Searchable> List<String> toINSERTTransaction(E item) {
        Class<? extends Searchable> eClass = item.getClass();
        List<String> queries = new ArrayList<>();
        List<Method> columnGetters = getColumnGetters(eClass);
        List<Method> tableRefGetters = getTableGetters(eClass);

        String columns = "(";
        String values = "VALUES (";
        String delim = "";

        // todo figure out if INSERT or UPDATE

        // generate query for this item
        for (Method column : columnGetters) {
            try {
                Object attribute = column.invoke(item);

                // todo add to String - Object hashmap instead
                // if not null ,add to insert statement
                if (attribute != null) {
                    columns += delim + ((Column) column.getDeclaredAnnotations()[0]).name();
                    values += delim
                            + (numericDataTypes.contains(attribute.getClass()) ? "" : "'")
                            + attribute.toString()
                            + (numericDataTypes.contains(attribute.getClass()) ? "" : "'");
                    delim = ", ";
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                System.err.println(e.getMessage());
            }
        }
        queries.add("INSERT INTO " + eClass.getSimpleName() + columns + ") " + values + ");");

        // todo how to handle references to parent items
        // recurse on children
        for (Method tableRef : tableRefGetters) {
            try {
                Object attribute = tableRef.invoke(item);
                TableReference tableRefAnn = (TableReference) tableRef.getDeclaredAnnotations()[0];

                // item is list
                if (attribute != null && attribute instanceof List<?>) {
                    List attributeList = ArrayList.class.cast(attribute);
                    for (int i = 0; i < attributeList.size(); i++) {
                        for (Class<? extends Searchable> type : tableRefAnn.specTypes()) {
                            queries.addAll(toINSERTTransaction(type.cast(attributeList.get(i))));
                        }
                    }
                }
                // item is individual
                else if (attribute != null) {
                    for (Class<? extends Searchable> type : tableRefAnn.specTypes()) {
                        queries.addAll(toINSERTTransaction(type.cast(attribute)));
                    }
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                System.err.println(e.getMessage());
            }
        }
        return queries;
    }

    /**
     * Converts a criterion into a deletion transaction
     */
    <E extends Searchable> List<String> toDELETETransaction(Criterion<E> criteria, DatabaseRepository persistence) {
        final String DELETESTRING = "DELETE FROM ";

        List<String> queries = new ArrayList<>();
        List<Method> complexGetters = getTableGetters(criteria.getCriterionClass());
        List<E> targets = persistence.getByCriteria(criteria);

        if (targets.size() == 0) return queries;

        // form delete transaction for specified item
        queries.add(DELETESTRING + criteria.getCriterionClass().getSimpleName()
                + (criteria.toString().equals("") ? "" : " WHERE ")
                + criteria.toString() + ";");

        for (E target : targets) {
            // issue delete transaction for complex children
            for (Method method : complexGetters) {
                TableReference annotation = (TableReference) method.getDeclaredAnnotations()[0];
                try {
                    Object attribute = method.invoke(target);
                    if (attribute == null) break;

                    // is list
                    if (attribute instanceof List<?>) {
                        List list = ArrayList.class.cast(attribute);
                        // for each list element, issue delete transaction
                        for (int i = 0; i < list.size(); i++) {
                            for (Class<? extends Searchable> type : annotation.specTypes()) {
                                queries.addAll(toDELETETransaction(
                                        new Criterion<>(type,
                                                ((Column) getPrimaryKeyGetter(list.get(i).getClass()).getDeclaredAnnotations()[0]).name(),
                                                EqualTo,
                                                getPrimaryKeyGetter(list.get(i).getClass()).invoke(list.get(i))), persistence));
                            }
                        }
                    }
                    // is not list
                    else {
                        for (Class<? extends Searchable> type : annotation.specTypes()) {
                            queries.addAll(toDELETETransaction(
                                    new Criterion<>(type,
                                            ((Column) getPrimaryKeyGetter(attribute.getClass()).getDeclaredAnnotations()[0]).name(),
                                            EqualTo,
                                            getPrimaryKeyGetter(attribute.getClass()).invoke(attribute)), persistence));
                        }
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    System.err.print(e.getMessage() + "DELETE: invoke @TableReference getter.");
                    return null;
                }
            }
        }
        return queries;
    }

    /**
     * Converts a ResultSet into a List<E> of objects
     */
    <E extends Searchable> List<E> toObjects(Class<E> eClass, ResultSet results, DatabaseRepository persistence) {
        List<E> returnList = new ArrayList<>();

        // reflection class data
        Constructor<E> constructor;
        Annotation[][] constructorAnnotations;
        Class<?>[] constructorArgumentTypes;

        // results metadata
        int columnNumber;
        List<String> columnNames;

        // obtain results metadata
        try {
            columnNumber = results.getMetaData().getColumnCount();
            columnNames = new ArrayList<>();
            for (int i = 1; i <= columnNumber; i++)
                columnNames.add(results.getMetaData().getColumnName(i));
        } catch (SQLException e) {
            System.err.print(e.getMessage() + " - Failed to obtain results metadata.");
            return null;
        }

        // obtain reflection data
        try {
            constructorArgumentTypes = new Class<?>[0];
            for (Constructor<?> c : eClass.getConstructors()) {
                if (c.getDeclaredAnnotations()[0].annotationType().equals(Reflective.class)) {
                    constructorArgumentTypes = c.getParameterTypes();
                    break;
                }
            }
            constructor = eClass.getConstructor(constructorArgumentTypes);
            constructorAnnotations = constructor.getParameterAnnotations();
        } catch (NoSuchMethodException e) {
            System.err.print(e.getMessage() + " - Failed to obtain constructor and parameters.");
            return null;
        }

        // map results to objects and add
        try {
            // for each row of resultset,
            while (results.next()) {
                Object[] initArgs = new Object[constructorAnnotations.length];

                // columns of ResultSet (constructor arguments)
                for (int i = 0; i < constructorAnnotations.length; i++) {
                    Annotation annotation = constructorAnnotations[i][0];

                    if (annotation.annotationType().equals(Column.class)) {
                        Column metadata = (Column) annotation;
                        String columnName = metadata.name();
                        int columnIndex = columnNames.indexOf(columnName) + 1;

                        /* This procedure is vulnerable to changes in the names of
                        enumerated types used in the software, as well as to changes
                        in the classes used in the program. If problems arise, make
                        sure all expected data types have a switch case below.
                        todo make more elegant by changing to something without switch
                         */
                        switch (constructorArgumentTypes[i].getSimpleName()) {
                            case "String":
                                initArgs[i] = results.getString(columnIndex);
                                break;
                            case "int":
                                initArgs[i] = results.getInt(columnIndex);
                                break;
                            case "double":
                                initArgs[i] = results.getDouble(columnIndex);
                                break;
                            case "float":
                                initArgs[i] = results.getFloat(columnIndex);
                                break;
                            case "boolean":
                                initArgs[i] = results.getBoolean(columnIndex);
                                break;
                            case "CustomerType": // todo fall-through behavior with Enum.valueOf
                                initArgs[i] = CustomerType.valueOf(results.getString(columnIndex));
                                break;
                            case "FuelType":
                                initArgs[i] = FuelType.valueOf(results.getString(columnIndex));
                                break;
                            case "UserType":
                                initArgs[i] = UserType.valueOf(results.getString(columnIndex));
                                break;
                            case "VehicleType":
                                initArgs[i] = VehicleType.valueOf(results.getString(columnIndex));
                                break;
                            case "Date":
                                initArgs[i] = new Date(results.getLong(columnIndex));
                                break;
                            case "DateTime":
                                initArgs[i] = new DateTime(results.getLong(columnIndex));
                                break;
                            default:
                                System.err.print("\nORM toObjects(): data type of constructor argument for database column "
                                        + "(" + columnIndex + ", " + columnName + ") could not be identified."
                                        + "Check DatabaseRepository.toObjects for missing switch cases.");
                                return null;
                        }
                    } else if (annotation.annotationType().equals(TableReference.class)) {
                        TableReference metadata = (TableReference) annotation;

                        // the complex attribute is a list of some form
                        if (constructorArgumentTypes[i].isAssignableFrom(List.class)) {

                            List<Object> finalResult = new ArrayList<>();

                            // fetch complex attribute data
                            for (Class<? extends Searchable> type : metadata.specTypes()) {
                                List<? extends Searchable> result = persistence.getByCriteria(new Criterion<>(type, metadata.key(),
                                        EqualTo, results.getObject(1).getClass().cast(results.getObject(1))));

                                if (result.size() > 0) {
                                    finalResult.addAll(result);
                                }
                            }
                            initArgs[i] = finalResult;
                        }
                        // the complex attribute is not a list
                        else {
                            for (Class<? extends Searchable> type : metadata.specTypes()) {
                                List<? extends Searchable> result = persistence.getByCriteria(new Criterion<>(type, metadata.key(),
                                        EqualTo, results.getObject(1).getClass().cast(results.getObject(1))));

                                // stop searching when single attribute is found
                                if (result.size() == 0) initArgs[i] = null;
                                else {
                                    initArgs[i] = result.get(0);
                                    break;
                                }
                            }
                        }
                    } else {
                        System.out.println("Annotation type not detected");
                    }
                }
                returnList.add(constructor.newInstance(initArgs));
            }
            return returnList;
        } catch (SQLException e) {
            System.err.print(e.getMessage() + " - Failed to map results to object.");
            return null;
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            System.err.print(e.getMessage() + " - Failed to create object instance.");
            return null;
        }
    }

    /**
     * Returns all reflective @Column getter methods for the class
     */
    private List<Method> getColumnGetters(Class<?> eClass) {
        List<Method> methods = new ArrayList<>(Arrays.asList(eClass.getDeclaredMethods()));

        // discard non-public, non-reflective methods and return reflective getters
        for (int i = 0; i < methods.size(); i++) {
            Method method = methods.get(i);
            if (!(Modifier.isPublic(method.getModifiers())
                    && method.getDeclaredAnnotations().length != 0
                    && method.getDeclaredAnnotations()[0].annotationType().equals(Column.class))) {
                methods.remove(method);
                i--;
            }
        }
        return methods;
    }

    /**
     * Returns all reflective @TableReference getter methods for the class
     */
    private List<Method> getTableGetters(Class<?> eClass) {
        List<Method> methods = new ArrayList<>(Arrays.asList(eClass.getDeclaredMethods()));

        // discard non-public, non-reflective methods and return @TableReference getters
        for (int i = 0; i < methods.size(); i++) {
            Method method = methods.get(i);
            if (!(Modifier.isPublic(method.getModifiers())
                    && method.getDeclaredAnnotations().length != 0
                    && method.getDeclaredAnnotations()[0].annotationType().equals(TableReference.class))) {
                methods.remove(method);
                i--;
            }
        }
        return methods;
    }

    /**
     * Returns getter for primary key
     */
    private Method getPrimaryKeyGetter(Class<?> eClass) {
        List<Method> methods = Arrays.asList(eClass.getDeclaredMethods());

        // return getter for primary key
        for (Method method : methods) {
            if (Modifier.isPublic(method.getModifiers())
                    && method.getDeclaredAnnotations().length != 0
                    && method.getDeclaredAnnotations()[0].annotationType().equals(Column.class)
                    && ((Column) method.getDeclaredAnnotations()[0]).primary())
                return method;
        }
        System.err.println("getPrimaryKeyGetter: return NULL on " + eClass.getSimpleName());
        return null;
    }
}