package entities;

import logic.Searchable;

import java.util.List;

/**
 * @author muradahmed
 * @version 0.1
 * @since : 0.1
 */

public class SpecialistRepairCenter implements Searchable {
    private int spcID;
    private String name;
    private String address;
    private String phone;
    private String email;

    // hierarchical links
    private List<SpecRepBooking> bookings;


    /**
     * @param spcID   Unique ID for a particular Specialist Repair Center
     * @param name    Name of a particular Specialist Repair Center
     * @param address Address of a particular Specialist Repair Center
     * @param phone   Phone number of a particular Specialist Repair Center
     * @param email   Email address of a particular Specialist Repair Center
     */
    @Reflective
    public SpecialistRepairCenter(@Column(name = "spcID", primary = true) int spcID,
                                  @Column(name = "name") String name,
                                  @Column(name = "address") String address,
                                  @Column(name = "phone") String phone,
                                  @Column(name = "email") String email,
                                  @TableReference(baseType = SpecRepBooking.class, specTypes = SpecRepBooking.class, key = "spcID")
                                          List<SpecRepBooking> bookings) {
        this.spcID = spcID;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.bookings = bookings;
    }


    /**
     * Returns a int representing Specialist Repair Center ID
     *
     * @return spcID
     */
    @Column(name = "spcID", primary = true)
    public int getSpcID() {
        return this.spcID;
    }

    /**
     * Returns a String representing Specialist Repair Center Name
     *
     * @return spcName
     */
    @Column(name = "name")
    public String getName() {
        return name;
    }

    /**
     * Returns a String representing Specialist Repair Center Address
     *
     * @return spcAddress
     */
    @Column(name = "address")
    public String getAddress() {
        return this.address;
    }

    /**
     * Returns a String representing Specialist Repair Center Phone number
     *
     * @return spcPhone
     */
    @Column(name = "phone")
    public String getPhone() {
        return this.phone;
    }

    /**
     * Returns a String representing Specialist Repair Center Email Address
     *
     * @return spcEmail
     */
    @Column(name = "email")
    public String getEmail() {
        return this.email;
    }
}