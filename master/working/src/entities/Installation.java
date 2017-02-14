package entities;


import java.util.Date;

/**
 * Created by shakib on 14/02/2017.
 */
public class Installation {

    private int installationID;
    private Date installationDate;
    private Date endWarrantyDate;

    // hierarchical links
    private PartOccurrence partOccurrence;

    // inverse hierarchical database links
    private String vehicleRegNumber;
    private int partAbstractionID;

    private Installation(int installationID, Date installationDate, Date endWarrantyDate, String vehicleRegNumber,
                         int partAbstractionID, @Complex(tableName = "PartOccurrence") PartOccurrence partOccurrence){
        this.installationID = installationID;
        this.installationDate = installationDate;
        this.endWarrantyDate = endWarrantyDate;
        this.vehicleRegNumber = vehicleRegNumber;
        this.partAbstractionID = partAbstractionID;
        this.partOccurrence = partOccurrence;
    }

    // todo getters and setters
    private int installationID(){
        return installationID;
    };
    private Date installationDate(){
        return installationDate;
    }
    private Date endWarrantyDate(){
        return endWarrantyDate;
    }
    private String vehicleRegNumber(){
        return vehicleRegNumber;
    }
}
