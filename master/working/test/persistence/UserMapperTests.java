package persistence;

import entities.Booking;
import entities.User;
import entities.UserType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Marcello De Bernardi
 * @version 0.1
 * @since 0.1
 */
public class UserMapperTests {
    private MapperFactory factory = MapperFactory.getInstance();


    /** Tests that MapperFactory returns the correct Mapper instances */
    @Test
    public void mapperFactoryTest() {
        assertTrue(factory.getMapper(User.class).getClass().equals(UserMapper.class) &&
                (factory.getMapper(Booking.class).getClass().equals(BookingMapper.class)));
    }

    /** Test SELECT statements returned by Mappers */
    @Test
    public void testUserSELECTQuery() {
        List<User> userList = new ArrayList<>();
        userList.add(new User(
                "gooby",
                "dolan",
                "Ebube",
                "Abara",
                UserType.NORMAL ));
        userList.add(new User(
                "someUserID",
                "somePassword",
                "Marcello",
                "De Bernardi",
                UserType.ADMINISTRATOR ));

        System.out.println(factory.getMapper(User.class).toSelectQuery(userList));
    }

    /**
     * Test INSERT statement returned by UserMapper
     */
    @Test
    public void testUserINSERTQuery() {
        User user = new User(
                "user",
                "password",
                "Uncle",
                "Dolan",
                UserType.ADMINISTRATOR);

        assertTrue(factory.getMapper(User.class).toInsertQuery(user)
                .equals("INSERT INTO User VALUES (user, password, Uncle, Dolan, ADMINISTRATOR);"));
    }

    /**
     * Tests DELETE statement returned by UserMapper
     */
    @Test
    public void testUserDELETEQUery() {
        User user = new User(
                "uniqueID",
                null,
                null,
                null,
                null
        );
        assertTrue(factory.getMapper(User.class).toDeleteQuery(user)
                .equals("DELETE FROM User WHERE userID = 'uniqueID';"));
    }
}