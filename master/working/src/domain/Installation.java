package domain;


import logic.criterion.Criterion;
import logic.criterion.CriterionOperator;
import persistence.DatabaseRepository;

import java.util.Date;
import java.util.List;

/**
 * Created by shakib on 14/02/2017.
 */
public class Installation implements Searchable {

    private int installationID;
    private Date installationDate;
    private Date endWarrantyDate;

    // hierarchical links
    private PartOccurrence partOccurrence;

    // inverse hierarchical database links
    private String vehicleRegNumber;
    private int partAbstractionID;


    /**
     * Creates a new installation
     *
     * @param installationDate
     * @param endWarrantyDate
     * @param vehicleRegNumber
     * @param partAbstractionID
     * @param partOccurrence
     */
    public Installation(Date installationDate, Date endWarrantyDate, String vehicleRegNumber,
                        int partAbstractionID, PartOccurrence partOccurrence) {
        this.installationID = -1;
        this.installationDate = installationDate;
        this.endWarrantyDate = endWarrantyDate;
        this.vehicleRegNumber = vehicleRegNumber;
        this.partAbstractionID = partAbstractionID;
        this.partOccurrence = partOccurrence;
    }

    // reflection only, do not use
    @Reflective
    private Installation(@Column(name = "installationID", primary = true) int installationID,
                         @Column(name = "installationDate") Date installationDate,
                         @Column(name = "endWarrantyDate") Date endWarrantyDate,
                         @Column(name = "vehicleRegNumber") String vehicleRegNumber,
                         @Column(name = "partAbstractionID") int partAbstractionID,
                         @TableReference(baseType = PartOccurrence.class, subTypes = PartOccurrence.class, key = "installationID")
                                 PartOccurrence partOccurrence) {
        this.installationID = installationID;
        this.installationDate = installationDate;
        this.endWarrantyDate = endWarrantyDate;
        this.vehicleRegNumber = vehicleRegNumber;
        this.partAbstractionID = partAbstractionID;
        this.partOccurrence = partOccurrence;
    }


    @Column(name = "installationID", primary = true)
    public int getInstallationID() {
        return installationID;
    }

    @Column(name = "installationDate")
    public Date getInstallationDate() {
        return installationDate;
    }

    @Column(name = "endWarrantyDate")
    public Date getEndWarrantyDate() {
        return endWarrantyDate;
    }

    @Column(name = "vehicleRegNumber")
    public String getVehicleRegNumber() {
        return vehicleRegNumber;
    }

    @Column(name = "partAbstractionID")
    public int getPartAbstractionID() {
        return partAbstractionID;
    }

    @TableReference(baseType = Installation.class, subTypes = PartOccurrence.class, key = "installationID")

    public PartOccurrence getPartOccurrence() {
        return partOccurrence;
    }

    @Lazy
    public Customer getCustomer() {
        List<Vehicle> vehicles = DatabaseRepository.getInstance().getByCriteria(new Criterion<>(Vehicle.class, "regNumber",
                CriterionOperator.EqualTo, getVehicleRegNumber()));

        if (vehicles.size() != 0) {
            List<Customer> customers = DatabaseRepository.getInstance().getByCriteria(new Criterion<>(Customer.class, "customerID",
                    CriterionOperator.EqualTo, vehicles.get(0).getCustomerID()));

            if (customers.size() != 0) return customers.get(0);
        }
        return null;
    }


    public void setInstallationID(int installationID) {
        this.installationID = installationID;
    }

    public void setInstallationDate(Date installationDate) {
        this.installationDate = installationDate;
    }

    public void setEndWarrantyDate(Date endWarrantyDate) {
        this.endWarrantyDate = endWarrantyDate;
    }

    public void setVehicleRegNumber(String vehicleRegNumber) {
        this.vehicleRegNumber = vehicleRegNumber;
    }

    public void setPartAbstractionID(int partAbstractionID) {
        this.partAbstractionID = partAbstractionID;
    }

    public void setPartOccurrence(PartOccurrence partOccurrence) {
        this.partOccurrence = partOccurrence;
    }

    public int getPartOccurrence(PartOccurrence partOccurrence) {
        return partOccurrence.getPartOccurrenceID();
    }

}
