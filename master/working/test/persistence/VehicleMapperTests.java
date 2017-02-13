package persistence;

import entities.FuelType;
import entities.Vehicle;
import entities.VehicleType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;


/**
 * @author Marcello De Bernardi
 */
public class VehicleMapperTests {
    private MapperFactory factory = MapperFactory.getInstance();

    /** Test SELECT statements returned by Mappers */
    @Test
    public void testVehicleSELECTQuery() {
        List<Vehicle> vehicleList = new ArrayList<>();
        vehicleList.add(new Vehicle(
                "AAA BBB",
                100,
                VehicleType.Car,
                "Jazz",
                "Honda",
                200,
                FuelType.diesel,
                "Celeste",
                30000,
                new Date(100),
                new Date(150),
                true,
                "SomeCompany",
                "Company Address 15",
                new Date(200)
                ));
        vehicleList.add(new Vehicle(
                "AAA BBB",
                100,
                VehicleType.Car,
                "Jazz",
                "Honda",
                200,
                FuelType.diesel,
                "Celeste",
                30000,
                new Date(100),
                new Date(150),
                true,
                "SomeCompany",
                "Company Address 15",
                new Date(200)
        ));

        System.out.println(factory.getMapper(Vehicle.class).toSELECTQuery(null));
        assertTrue(factory.getMapper(Vehicle.class).toSELECTQuery(null)
                .equals("SELECT * FROM Vehicle WHERE (regNumber = 'AAA BBB' AND customerID = 100"
                        + " AND vehicleType = 'Car' AND model = 'Jazz' AND manufacturer = 'Honda'"
                        + " AND engineSize = 200.0 AND fuelType = 'diesel' AND colour = 'Celeste'"
                        + " AND mileage = 30000 AND renewalDateMot = 100 AND dateLastServiced = 150"
                        + " AND coveredByWarranty = 1 AND warrantyName = 'SomeCompany' AND "
                        + "warrantyCompName = 'Company Address 15' AND warrantyExpirationDate = 200)"
                        + " OR (regNumber = 'AAA BBB' AND customerID = 100 AND vehicleType = 'Car' "
                        + "AND model = 'Jazz' AND manufacturer = 'Honda' AND engineSize = 200.0 AND "
                        + "fuelType = 'diesel' AND colour = 'Celeste' AND mileage = 30000 AND "
                        + "renewalDateMot = 100 AND dateLastServiced = 150 AND coveredByWarranty = 1"
                        + " AND warrantyName = 'SomeCompany' AND warrantyCompName = 'Company Address 15'"
                        + " AND warrantyExpirationDate = 200);"));
    }

    /**
     * Test INSERT statement returned by UserMapper
     */
    @Test
    public void testVehicleINSERTQuery() {
        Vehicle vehicle = new Vehicle(
                "AAA BBB",
                100,
                VehicleType.Car,
                "Jazz",
                "Honda",
                200,
                FuelType.diesel,
                "Celeste",
                30000,
                new Date(100),
                new Date(150),
                true,
                "SomeCompany",
                "Company Address 15",
                new Date(200)
        );

        System.out.println(factory.getMapper(Vehicle.class).toINSERTTransaction(vehicle));
        assertTrue(factory.getMapper(Vehicle.class).toINSERTTransaction(vehicle)
                .equals("INSERT INTO Vehicle VALUES ('AAA BBB', 100, 'Car', 'Jazz', 'Honda', 200.0, 'diesel', "
                        + "'Celeste', 30000, 100, 150, 1, 'SomeCompany', 'Company Address 15', 200);"));
    }

    /**
     * Test UPDATE statement returned by UserMapper
     */
    @Test
    public void testVehicleUPDATEQuery() {
        Vehicle vehicle = new Vehicle(
                "AAA BBB",
                100,
                VehicleType.Car,
                "Jazz",
                "Honda",
                200,
                FuelType.diesel,
                "Celeste",
                30000,
                new Date(100),
                new Date(150),
                true,
                "SomeCompany",
                "Company Address 15",
                new Date(200)
        );

        System.out.println(factory.getMapper(Vehicle.class).toUPDATETransaction(vehicle));

        assertTrue(factory.getMapper(Vehicle.class).toUPDATETransaction(vehicle)
                .equals("UPDATE Vehicle SET customerID = 100, vehicleType = 'Car', model = 'Jazz', "
                        + "manufacturer = 'Honda', engineSize = 200.0, fuelType = 'diesel', colour = 'Celeste',"
                        + " mileage = 30000, renewalDateMot = 100, dateLastServiced = 150, "
                        + "coveredByWarranty = 1, warrantyName = 'SomeCompany', "
                        + "warrantyCompAddress = 'Company Address 15', warrantyExpirationDate = 200"
                        + " WHERE regNumber = 'AAA BBB';"));
    }

    /**
     * Tests DELETE statement returned by UserMapper
     */
    @Test
    public void testVehicleDELETEQuery() {
        Vehicle vehicle = new Vehicle(
                "AAA BBB",
                -1,
                null,
                null,
                null,
                -1,
                null,
                null,
                -1,
                null,
                null,
                false,
                null,
                null,
                null
        );

        System.out.println(factory.getMapper(Vehicle.class).toDELETETransaction(null));

        assertTrue(factory.getMapper(Vehicle.class).toDELETETransaction(null)
                .equals("DELETE FROM Vehicle WHERE regNumber = 'AAA BBB';"));
    }
}
