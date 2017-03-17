package logic.parts;

import domain.PartAbstraction;
import domain.PartOccurrence;
import logic.criterion.Criterion;
import logic.criterion.CriterionRepository;
import persistence.DatabaseRepository;
import static logic.criterion.CriterionOperator.*;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by Muhammad Shakib Hoque on 07/02/2017.
 *
 */
public class PartsSystem {

    private static PartsSystem instance;
    private CriterionRepository persistence = DatabaseRepository.getInstance();
    private PartAbstraction addNewPart;

    private PartsSystem(CriterionRepository persistence) {

        this.persistence = persistence;
    }

    public static PartsSystem getInstance(CriterionRepository persistence) {
        if (instance == null) instance = new PartsSystem(persistence);
        return instance;
    }

    public List<PartAbstraction> getPartList() {

        ArrayList<PartAbstraction> arrayPartsList = new ArrayList<PartAbstraction>();
        return arrayPartsList;

    }

    public PartAbstraction searchPartList(String partName) {
        List<PartAbstraction> result = persistence.getByCriteria(new Criterion<>(PartAbstraction.class, "partName", EqualTo, partName));
        return result.size() == 0 ? null : result.get(0);

    }

    public boolean addPart(String partName, String partDescription, Double partPrice, int partStockLevel){

        PartAbstraction addNewPart = new PartAbstraction(partName, partDescription, partPrice, partStockLevel, null);
        boolean result = persistence.commitItem(addNewPart);
        return result;

    }

    public boolean deletePart(int partAbstractionID){

        return persistence.deleteItem(new Criterion<>(PartAbstraction.class, "partAbstractionID", EqualTo, partAbstractionID));

    }

    public boolean editPart(){

        return false;

    }

    public PartAbstraction getPartbyID(int partAbstractionID) {
        List<PartAbstraction> result = persistence.getByCriteria(new Criterion<>(PartAbstraction.class, "partAbstractionID", EqualTo, partAbstractionID));
        return result.size() == 0 ? null : result.get(0);

    }

    public boolean addPartOccurrence(PartOccurrence partOccurrence)
    {
        return persistence.commitItem(partOccurrence);
    }

    public List<PartAbstraction> getByName(String query)
    {
        return persistence.getByCriteria(new Criterion<>(PartAbstraction.class, "partName",EqualTo, query));
    }
    public List<PartOccurrence> getAllFreeOccurrences()
    {
        return persistence.getByCriteria(new Criterion<>(PartOccurrence.class, "installationID", EqualTo,0));
    }


}


