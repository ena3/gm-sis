package logic;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * <p>
 *     Criterion objects are used to interact with CriterionRepositories. They encapsulate criteria
 *     by which entities in the persistence layer are identified. A Criterion is created like this:
 * </p>
 * <p>
 *     <i>new Criterion(MyClass.class, "attributeName", operator, value)</i>
 * </p>
 * <p>
 *     where MyClass is the class of relevant objects (bookings, vehicles, etc), "attributeName" is
 *     a string matching exactly the name of a field in said class, value is an object of valid type
 *     for said field, and operator is one of the four operators defined in the CriterionOperator enum.
 * </p>
 * <p>
 *     The four operators are LessThan, MoreThan, EqualTo and Regex. Regex can only be used when the
 *     value object is of type String or type Pattern. The other operators can be used unrestrictedly.
 * </p>
 * <p>
 *     More complex search criteria can be used with the append methods and(), or(), and setDiff().
 *     These methods are called on an existing criterion, and are used to add a new set of criteria
 *     to the Criterion with the same argument syntax used in the constructor, minus the class
 *     specification. The new set of criteria is connected to the previous one with the logical
 *     connective specified in the method name. For example,
 * </p>
 * <p>
 *     <i>new Criterion<>(MyClass.class, "att", LessThan, 10).and("att", MoreThan, 5)</i>
 * </p>
 * <p>
 *     defines the expression
 * </p>
 * <p>
 *     <i>(att < 10) AND (att > 5)</i>
 * </p>
 * <p>
 *     More complex queries can be formed by combining attributes, values and operators with the
 *     provided logical connectives.
 * </p>
 * <p>
 *     Note that several methods in this class throw CriterionException, which IS NOT A CHECKED
 *     EXCEPTION. That is, you do not have to explicitly catch it; however, you need to ensure that
 *     you write your criteria properly, so that it is not thrown.
 * </p>
 *
 * @author Marcello De Bernardi
 * @version 0.2
 * @since 0.1
 */
public class Criterion<E extends Searchable> {
    private Class<E> eClass;
    private List<Field> fields;
    private List<String> attributes;
    private List<CriterionOperator> operators;
    private List<Object> values;
    private List<String> logicalConnectives;
    // todo add immutable flag, and constructor that allows for null fields


    /**
     * Creates a new Criterion object. The Class has to match the type of object wanted
     * in return. The attribute string has to match one of the non-inherited fields in
     * the class, and the value must be an object of the type of the field in the class
     * (a wrapper object in case the field is primitive).
     *
     * @param attribute object variable used as search criterion
     * @param operator  operator applied to value
     * @param value     value for search variable
     * @throws CriterionException
     */
    public Criterion(Class<E> eClass, String attribute, CriterionOperator operator, Object value)
            throws CriterionException {
        // check compatibility of operator and value
        if (!operatorIsCompatible(operator, value)) throw new CriterionException();

        // extract fields and check if arguments are compatible
        this.eClass = eClass;
        fields = Arrays.asList(this.eClass.getFields());
        for (Field f : fields) {
            if (f.getName().equals(attribute) && (value.getClass().equals(f.getType()))) {
                attributes = Collections.singletonList(attribute);
                operators = Collections.singletonList(operator);
                values = Collections.singletonList(value);
                logicalConnectives = new ArrayList<>();
                return;
            }
        }

        // throw error if criteria not acceptable
        throw new CriterionException();
    }

    /**
     * Adds a criterion to the Criterion object, connected to previous criteria by an AND
     * logical connective.
     *
     * @param attribute attribute to add to the criterion
     * @param operator  operator connecting attribute to value
     * @param value     value of attribute
     * @return modified Criterion object
     * @throws CriterionException if poorly specified arguments
     */
    public Criterion<E> and(String attribute, CriterionOperator operator, Object value)
            throws CriterionException {
        // check compatibility of operator and value
        if (!operatorIsCompatible(operator, value)) throw new CriterionException();

        // check criteria if acceptable
        if (isClassCompatible(attribute, value)) {
            attributes.add(attribute);
            operators.add(operator);
            values.add(value);
            logicalConnectives.add("AND");
            return this;
        }

        // throw error if criteria not acceptable
        throw new CriterionException();
    }

    /**
     * Adds a criterion to the Criterion object, connected to previous criteria by an OR
     * logical connective.
     *
     * @param attribute attribute to add to the criterion
     * @param operator  operator connecting attribute to value
     * @param value     value of attribute
     * @return modified Criterion object
     * @throws CriterionException if poorly specified arguments
     */
    public Criterion<E> or(String attribute, CriterionOperator operator, Object value)
            throws CriterionException {
        // check compatibility of operator and value
        if (!operatorIsCompatible(operator, value)) throw new CriterionException();

        // check criteria if acceptable
        if (isClassCompatible(attribute, value)) {
            attributes.add(attribute);
            operators.add(operator);
            values.add(value);
            logicalConnectives.add("OR");
            return this;
        }

        // throw error if criteria not acceptable
        throw new CriterionException();
    }

    /**
     * Adds a criterion to the Criterion object, connected to previous criteria by a SET DIFFERENCE
     * set operator.
     *
     * @param attribute attribute to add to the criterion
     * @param operator  operator connecting attribute to value
     * @param value     value of attribute
     * @return modified Criterion object
     * @throws CriterionException if poorly specified arguments
     */
    public Criterion<E> setDiff(String attribute, CriterionOperator operator, Object value)
            throws CriterionException {
        // check compatibility of operator and value
        if (!operatorIsCompatible(operator, value)) throw new CriterionException();

        // check criteria if acceptable
        if (isClassCompatible(attribute, value)) {
            attributes.add(attribute);
            operators.add(operator);
            values.add(value);
            logicalConnectives.add("MINUS");
            return this;
        }

        // throw error if criteria not acceptable
        throw new CriterionException();
    }

    /**
     * <p>
     *     Returns a string representing the collected criteria in the following form:
     * </p>
     * <p>
     *     <i>ATTRIBUTE1 OPERATOR VALUE</i>    - for a single criterion
     * </p>
     * <p>
     *     <i>(ATTRIBUTE1 OPERATOR VALUE) CONNECTIVE (ATTRIBUTE2 OPERATOR VALUE)</i>
     *     - for multiple criteria
     * </p>
     * <p>
     *     Mostly intended for use by the persistence layer, but can also be used to
     *     check validity of criteria.
     * </p>
     * @return String representing search criteria
     */
    public String toString() {
        String returnString = "" + attributes.get(0) + " " + operators.get(0) + " "
                + values.get(0);

        if (attributes.size() > 0) {
            returnString = "(" + returnString + ")";
            for (int i = 1, j = 0; i < attributes.size(); i++, j++) {
                returnString += " " + logicalConnectives.get(j) + " (" + attributes.get(i) + " "
                        + operators.get(i) + ")";
            }
        }

        return returnString;
    }

    /**
     * Returns the class associated with this Criterion.
     *
     * @return class of criterion
     */
    public Class<E> getCriterionClass() {
        return eClass;
    }

    // returns true if attribute and value are compatible with class of Criterion
    private boolean isClassCompatible(String attribute, Object value) {
        for (Field f : fields) {
            if (f.getName().equals(attribute) && (value.getClass().equals(f.getType())))
                return true;
        }
        return false;
    }

    // returns true if operator and value are compatible
    private boolean operatorIsCompatible(CriterionOperator operator, Object value) {
        return !(operator.equals(CriterionOperator.Regex) &&
                !(value.getClass().equals(String.class) || value.getClass().equals(Pattern.class)));
    }
}
