package logic;

import java.util.List;

/**
 * <p>
 * <i>PersistenceInterface</i> provides an abstraction of persistence implementations. Objects
 * implementing the interface fulfill the contract of accepting Criterion objects and returning
 * business entities. An implementation of the interface is specific both to a particular entity
 * model as well as to a particular persistence solution.
 * </p>
 *
 * @author Marcello De Bernardi
 * @version 0.2
 * @since 0.1
 */
public interface CriterionRepository {
    /**
     * <p>
     * Takes any number of Criterion objects and returns a List<E extends Searchable> of objects
     * stored in the persistence layer that match these criteria. The Criterion object itself
     * defines the base type of the returned object.
     * </p>
     * <p>
     * <i>new Criterion<>(User.class, "userID", EqualTo, "foo").and("password", EqualTo, "bar")</i>
     * </p>
     * <p>
     * corresponds to the logical statement
     * </p>
     * <p>
     * <i>Class = User AND userID = 'foo' AND password = 'bar'</i>
     * </p>
     *
     * @param criteria object defining set of criteria connected by AND logical connective
     * @return List of objects of same actual type as the passed working.logic.Criterion objects
     */
    <E extends Searchable> List<E> getByCriteria(Criterion<E> criteria);

    /**
     * <p>
     * Takes a single Searchable object and adds it to the persistence layer if it is not
     * already present (must be a new entity from a business perspective).
     * </p>
     * <p>
     * When inserting, insertion propagates downward in the entity hierarchy. Updates do not
     * propagate, but insertion triggers updates for any nodes in the hierarchy that are a single
     * link away from the inserted node. Further updates, if necessary, need to be performed manually.
     * </p>
     *
     * @param item Criterion object to add to the database
     * @return true if add successful, false is add unsuccessful
     */
    <E extends Searchable> boolean addItem(Class<E> eClass, E item);

    /**
     * /**
     * <p>
     * Takes a Searchable object and updates the information stored about it in the
     * persistence layer. The primary ID variable must not be missing.
     * </p>
     * <p>
     * Updates do not propagate, and must be performed manually if necessary.
     * </p>
     *
     * @param item Criterion object to add to the database
     * @return true if update successful, false is update rejected
     */
    <E extends Searchable> boolean updateItem(Class<E> eClass, E item);

    /**
     * <p>
     * Takes a Criterion object and deletes the item it specifies from the persistence layer,
     * Objects are identified in the persistence layer by their primary ID variable.
     * </p>
     * <p>
     * Deletion propagates downward in the entity hierarchy. Updates do not propagate, but
     * deletion triggers updates for entities upward of the deleted entity that are one
     * link away from it. Other updates must be performed manually if necessary.
     * </p>
     * <p>
     *     Regex operator should be avoided. Using the regex operator on identifiers can
     *     cause catastrophic loss of database contents.
     * </p>
     *
     * @param criteria Criterion objects to define the objects to remove
     * @return true if successful, false if unsuccessful
     */
    <E extends Searchable> boolean deleteItem(Criterion<E> criteria);
}
