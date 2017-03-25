package logic.booking;

import domain.Customer;
import domain.DiagRepBooking;
import domain.Mechanic;
import domain.Vehicle;
import logic.criterion.Criterion;
import logic.criterion.CriterionRepository;
import persistence.DatabaseRepository;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.time.DayOfWeek.*;
import static logic.booking.UnavailableDateException.Cause.CLASHES;
import static logic.booking.UnavailableDateException.Cause.CLOSED;
import static logic.booking.UnavailableDateException.Cause.HOLIDAY;
import static logic.criterion.CriterionOperator.*;

/**
 * @author Marcello De Bernardi
 * @version 0.1
 * @since 0.1
 */
public class BookingSystem {
    private static BookingSystem instance;
    private CriterionRepository persistence;
    private DateTimeFormatter format;

    private Map<LocalDate, String> holidays;
    private Map<DayOfWeek, LocalTime[]> openingHours;

    private BookingSystem() {
        this.persistence = DatabaseRepository.getInstance();
        format = DateTimeFormatter.ISO_LOCAL_DATE;

        openingHours = new HashMap<>();
        openingHours.put(MONDAY, new LocalTime[]{LocalTime.of(9,0), LocalTime.of(17,30)});
        openingHours.put(TUESDAY, new LocalTime[]{LocalTime.of(9,0), LocalTime.of(17,30)});
        openingHours.put(WEDNESDAY, new LocalTime[]{LocalTime.of(9,0), LocalTime.of(17,30)});
        openingHours.put(THURSDAY, new LocalTime[]{LocalTime.of(9,0), LocalTime.of(17,30)});
        openingHours.put(FRIDAY, new LocalTime[]{LocalTime.of(9,0), LocalTime.of(17,30)});
        openingHours.put(SATURDAY, new LocalTime[]{LocalTime.of(9,0), LocalTime.of(12,0)});
        openingHours.put(SUNDAY, new LocalTime[]{LocalTime.of(0,0), LocalTime.of(0,0)});

        holidays = new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader("master/working/config/holidays.txt"));
            reader.lines().filter(line -> line.length() > 0 && line.charAt(0) != '#' && line.charAt(0) != ' ')
                    .forEach(line -> holidays.put(
                            LocalDate.parse(line.split("\\s*!\\s*")[0], format),
                            line.split("\\s*!*\\s")[1]));
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the singleton instance of the BookingSystem.
     *
     * @return singleton instance of BookingSystem
     */
    public static BookingSystem getInstance() {
        if (instance == null) instance = new BookingSystem();
        return instance;
    }


    /**
     * Returns a list containing all bookings for diagnosis and repair.
     *
     * @return List of diagnosis and repair bookings
     */
    public List<DiagRepBooking> getAllBookings() {
        return persistence.getByCriteria(new Criterion<>(DiagRepBooking.class));
    }

    /**
     * Returns a list containing all diagnosis and repair bookings for which either the diagnosis
     * or the repair is yet to take place.
     *
     * @return list of future bookings
     */
    public List<DiagRepBooking> getFutureBookings() {
        ZonedDateTime now = ZonedDateTime.now();

        return persistence.getByCriteria(new Criterion<>(DiagRepBooking.class)
                .where("diagnosisStart", moreThan, now)
                .or("repairStart", moreThan, now)
        );
    }

    /**
     * Returns a list containing all diagnosis and repair bookings for which either the diagnosis
     * or the repair has already taken place.
     *
     * @return list of past bookings
     */
    public List<DiagRepBooking> getPastBookings() {
        ZonedDateTime now = ZonedDateTime.now();

        return persistence.getByCriteria(new Criterion<>(DiagRepBooking.class)
                .where("diagnosisStart", lessThan, now)
                .or("repairStart", lessThan, now)
        );
    }

    /**
     * Returns all diagnosis and repair bookings such that the booking has either a diagnosis
     * appointment or a repair appointment inside the specified time range.
     *
     * @param start start time of time range
     * @param end   end time of time range
     * @return list of bookings in range
     */
    public List<DiagRepBooking> getBookingsBetween(ZonedDateTime start, ZonedDateTime end) {
        return persistence.getByCriteria(new Criterion<>(DiagRepBooking.class)
                .where("diagnosisStart", moreThan, start)
                .and("diagnosisStart", lessThan, end)
                .or("repairStart", moreThan, start)
                .and("repairStart", lessThan, end)
        );
    }

    /**
     * Returns a list of all mechanics.
     *
     * @return list of mechanics
     */
    public List<Mechanic> getAllMechanics() {
        return persistence.getByCriteria(new Criterion<>(Mechanic.class));
    }

    /**
     * Returns a list of all bookings matching the query in one of several ways:
     * <p>
     * 1. the query is the bookingID of a booking
     * 2. the query can be pattern-matched to the vehicle registration number
     * 3. the query can be pattern-matched to the customer name todo
     * 4. the query can be pattern-matched to the mechanic name todo
     *
     * @param query query
     * @return list of bookings, may be empty
     */
    public List<DiagRepBooking> searchBookings(String query) {
        if (query == null) throw new NullPointerException();
        if (query.equals("")) return persistence.getByCriteria(new Criterion<>(DiagRepBooking.class));

        return persistence.getByCriteria(new Criterion<>(DiagRepBooking.class)
                .where("vehicleRegNumber", matches, query)
                .or("vehicleRegNumber", in, new Criterion<>(Vehicle.class)
                        .where("manufacturer", matches, query)
                        .or("customerID", in, new Criterion<>(Customer.class)
                                .where("customerFirstname", matches, query)
                                .or("customerSurname", matches, query))));
    }

    /**
     * Returns a booking as identified by its booking ID.
     *
     * @param bookingID ID of booking
     * @return booking, may be null
     */
    public DiagRepBooking getBookingByID(int bookingID) {
        List<DiagRepBooking> result = persistence.getByCriteria(
                new Criterion<>(DiagRepBooking.class).where("bookingID", equalTo, bookingID)
        );

        return result != null && result.size() != 0 ? result.get(0) : null;
    }

    /**
     * Returns a mechanic as identified by their ID.
     *
     * @param mechanicID ID of mechanic
     * @return mechanic, may be null
     */
    public Mechanic getMechanicByID(int mechanicID) {
        List<Mechanic> result = persistence.getByCriteria(
                new Criterion<>(Mechanic.class).where("mechanicID", equalTo, mechanicID));
        return result != null ? result.get(0) : null;
    }

    /**
     * Returns all bookings, including past and future, of a particular vehicle.
     *
     * @param regNumber the registration number of the vehicle
     * @return list of bookings for vehicle
     */
    public List<DiagRepBooking> getVehicleBookings(String regNumber) {
        return persistence.getByCriteria(
                new Criterion<>(DiagRepBooking.class).where("vehicleRegNumber", equalTo, regNumber));
    }

    /**
     * <p>
     * Adds a new booking to the persistence layer. Assumes an existing customer and
     * vehicle.
     * </p>
     *
     * @return true if addition successful, false otherwise
     */
    public boolean commitBooking(DiagRepBooking booking) throws UnavailableDateException {
        return !isClosed(booking) && !isHoliday(booking) && !clashes(booking) && persistence.commitItem(booking);
    }


    /**
     * Removes a specified booking from the system.
     *
     * @return true if removal successful, false otherwise
     */
    public boolean deleteBookingByID(int bookingID) {
        return persistence.deleteItem(
                new Criterion<>(DiagRepBooking.class).where("bookingID", equalTo, bookingID));
    }

    /** Checks time validity in terms of opening and closing hours as well as weekdays. */
    public boolean isClosed(DiagRepBooking booking) throws UnavailableDateException {
        DayOfWeek diagDay = booking.getDiagnosisStart().getDayOfWeek();
        LocalTime diagStart = booking.getDiagnosisStart().toLocalTime();
        LocalTime diagEnd = booking.getDiagnosisStart().toLocalTime();

        // if diagnosis time is outside opening hours throw exception
        if (diagStart.isBefore(openingHours.get(diagDay)[0]) || diagStart.isAfter(openingHours.get(diagDay)[1]))
            throw new UnavailableDateException().because(CLOSED).at(diagStart);
        else if (diagEnd.isBefore(openingHours.get(diagDay)[0]) || diagEnd.isAfter(openingHours.get(diagDay)[1]))
            throw new UnavailableDateException().because(CLOSED).at(diagEnd);

        // check repair time exists
        DayOfWeek repDay = booking.getRepairStart() == null ? booking.getRepairStart().getDayOfWeek() : null;
        LocalTime repStart = booking.getRepairStart() == null ? booking.getRepairStart().toLocalTime() : null;
        LocalTime repEnd = booking.getRepairStart() == null ? booking.getRepairStart().toLocalTime() : null;

        // if repair time exists and is outside opening hours throw exception
        if (repDay == null || repStart == null || repEnd == null)
            return false;
        else if (repStart.isBefore(openingHours.get(repDay)[0]) || repStart.isAfter(openingHours.get(repDay)[1]))
            throw new UnavailableDateException().because(CLOSED).at(repStart);
        else if (repEnd.isBefore(openingHours.get(repDay)[0]) || repEnd.isAfter(openingHours.get(repDay)[1]))
            throw new UnavailableDateException().because(CLOSED).at(repEnd);

        return false;
    }

    /** Checks the booking is not for a bank or public holiday */
    public boolean isHoliday(DiagRepBooking booking) throws UnavailableDateException {
        if (holidays.containsKey(booking.getDiagnosisStart().toLocalDate()))
            throw new UnavailableDateException()
                    .because(HOLIDAY)
                    .on(holidays.get(booking.getDiagnosisStart().toLocalDate()));
        else if (booking.getRepairStart() != null && holidays.containsKey(booking.getRepairStart().toLocalDate()))
            throw new UnavailableDateException()
                    .because(HOLIDAY)
                    .on(holidays.get(booking.getRepairStart().toLocalDate()));

        return false;
    }

    /** Checks that the booking does not clash temporally with other bookings */
    public boolean clashes(DiagRepBooking booking) throws UnavailableDateException {
        // todo rewrite
        List<DiagRepBooking> clashes = persistence.getByCriteria(new Criterion<>(DiagRepBooking.class)
                .where("diagnosisStart", after, booking.getDiagnosisStart())
                .and("diagnosisStart", before, booking.getDiagnosisEnd())
                .or("diagnosisStart", after, booking.getRepairStart())
                .and("diagnosisStart", before, booking.getRepairEnd())
                .or("diagnosisEnd", after, booking.getDiagnosisStart())
                .and("diagnosisEnd", before, booking.getDiagnosisEnd())
                .or("diagnosisEnd", after, booking.getRepairStart())
                .and("diagnosisEnd", before, booking.getRepairEnd())
                .or("repairStart", after, booking.getDiagnosisStart())
                .and("repairStart", before, booking.getDiagnosisEnd())
                .or("repairStart", after, booking.getRepairStart())
                .and("repairStart", before, booking.getRepairEnd())
                .or("repairEnd", after, booking.getDiagnosisStart())
                .and("repairEnd", before, booking.getDiagnosisEnd())
                .or("repairEnd", after, booking.getRepairStart())
                .and("repairEnd", before, booking.getRepairEnd())).stream()
                .filter(b -> b.getMechanicID() == booking.getMechanicID())
                .collect(Collectors.toList());

        if (clashes != null && clashes.size() != 0) throw new UnavailableDateException().because(CLASHES).with(clashes);
        return false;
    }
}
