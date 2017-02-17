package entities;

import logic.Searchable;

import java.util.List;

/**
 * @author Muhammad Shakib Hoque on 07/02/2017.
 */
public class PartAbstraction implements Searchable {

    private int partAbstractionID;
    private String partName;
    private String partDescription;
    private double partPrice;
    private int partStockLevel;

    // hierarchical links
    private List<PartOccurrence> occurrenceList;

    @Reflective
    public PartAbstraction(@Column(name = "partAbstractionID", primary = true) int partAbstractionID,
                           @Column(name = "partName") String partName,
                           @Column(name = "partDescription") String partDescription,
                           @Column(name = "partPrice") double partPrice,
                           @Column(name = "partStockLevel") int partStockLevel,
                           @TableReference(baseType = PartOccurrence.class, specTypes = PartOccurrence.class, key = "partAbstractionID")
                                   List<PartOccurrence> occurrenceList){
        this.partAbstractionID = partAbstractionID;
        this.partName = partName;
        this.partDescription = partDescription;
        this.partPrice = partPrice;
        this.partStockLevel = partStockLevel;
        this.occurrenceList = occurrenceList;
    }

    @Column(name = "partAbstractionID", primary = true)
    public int getPartAbstractionID() {
        return partAbstractionID;
    }

    public void setPartAbstractionID(int partAbstractionID) {
        this.partAbstractionID = partAbstractionID;
    }

    @Column(name = "partName")
    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }

    @Column(name = "partDescription")
    public String getPartDescription() {
        return partDescription;
    }

    public void setPartDescription(String partDescription) {
        this.partDescription = partDescription;
    }

    @Column(name = "partPrice")
    public double getPartPrice() {
        return partPrice;
    }

    public void setPartPrice(double partPrice) {
        this.partPrice = partPrice;
    }

    @Column(name = "partStockLevel")
    public int getPartStockLevel() {
        return partStockLevel;
    }

    public void setPartStockLevel(int partStockLevel) {
        this.partStockLevel = partStockLevel;
    }

    @TableReference(baseType = PartOccurrence.class, specTypes = PartOccurrence.class, key = "partAbstractionID")
    public List<PartOccurrence> getOccurrenceList() {
        return occurrenceList;
    }

    public void setOccurrenceList(List<PartOccurrence> occurrenceList) {
        this.occurrenceList = occurrenceList;
    }
}