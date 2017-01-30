package logic;

import java.util.List;

/**
 * <p>
 * <i>PersistenceInterface</i> provides an abstraction of persistence implementations. Objects
 * implementing the interface fulfill the contract of accepting Criterion objects and returning
 * Criterion objects. An implementation of the interface is specific both to a particular entity
 * model as well as to a particular persistence solution.
 * </p>
 *
 * @author Marcello De Bernardi
 * @version 0.1
 * @since 0.1
 */
public interface CriterionRepository {

    // TODO: consider alternative abstraction model and deprecation of criterion abstraction

    /**
     * <p>
     * Takes any number of Criterion objects and returns a SortedSet of objects stored in the
     * persistence layer that match these criteria. An individual Criterion object defines a set
     * of criteria connected by the AND logical operator, while multiple Criterion objects are
     * connected by the OR logical operator. For example:
     * </p>
     * <p>
     * <i>new Criterion("alpha", "beta", null), new Criterion ("ALPHA", "BETA", "GAMMA")</i>
     * </p>
     * <p>
     * corresponds to the logical statement
     * </p>
     * <p>
     * <i>("alpha" AND "beta") OR ("ALPHA" AND "BETA" AND "GAMMA")</i>
     * </p>
     * <p>
     * <p>The patternMatching flag determines whether certain attributes, such as names, are
     * to be interpreted as regular expressions. </p>
     *
     * @param patternMatching true for regular expression interpretation
     * @param criteria        object defining set of criteria connected by AND logical connective
     * @return SortedSet of objects of same actual type as the passed working.logic.Criterion objects
     */
    List<Criterion> getByCriteria(boolean patternMatching, Criterion... criteria);

    /**
     * <p>
     * Takes any number of Criterion objects and adds them all to the persistence layer if none of
     * them are already present (all objects must be new entities from a business perspective).
     * Using Criterion objects allows entities of different actual types to be added to the
     * persistence layer.
     * </p>
     * <p>
     * Only the top-level object in a hierarchy of new objects needs to be added.
     * </p>
     *
     * @param items Criterion objects to add to the database
     * @return true if add successful, false is add unsuccessful
     */
    boolean addItem(Criterion... items);

    /**
     * /**
     * <p>
     * Takes any number of Criterion objects and updates the information stored in the
     * persistence layer for each object. Objects are identified in the persistence layer
     * by their primary ID variable.
     * </p>
     * <p>
     * Only the top-level object in a hierarchy of updated objects need to be passed as argument.
     * </p>
     *
     * @param items Criterion objects to add to the database
     * @return true if update successful, false is update rejected
     */
    boolean updateByCriteria(Criterion... items);

    /**
     * <p>
     * Takes any number of Criterion objects and deletes them, as well as any subordinate objects,
     * from the persistence layer. Objects are identified in the persistence layer by their primary
     * ID variable.
     * </p>
     * <p>
     * Only the top-level object in a hierarchy of updated objects need to be passed as argument.
     * Subordinates are also deleted. Objects to which an argument object has no references are not
     * automatically deleted and need to be deleted manually.
     * </p>
     * <p>
     * The patternMatching flag can be set to allow for specific attributes to be interpreted
     * as regular expressions.
     * </p>
     *
     * @param patternMatching if true interpret fields as regular expressions
     * @param criteria Criterion objects to define the objects to remove
     * @return true if successful, false if unsuccessful
     */
    boolean deleteByCriteria(boolean patternMatching, Criterion... criteria);
}