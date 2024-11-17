package com.beanbeanjuice.cafeapi.beta;

import com.beanbeanjuice.cafeapi.wrapper.CafeAPI;
import com.beanbeanjuice.cafeapi.wrapper.endpoints.cafe.CafeType;
import com.beanbeanjuice.cafeapi.wrapper.endpoints.cafe.CafeUser;
import com.beanbeanjuice.cafeapi.wrapper.exception.api.ConflictException;
import com.beanbeanjuice.cafeapi.wrapper.exception.api.NotFoundException;
import com.beanbeanjuice.cafeapi.wrapper.exception.api.TeaPotException;
import com.beanbeanjuice.cafeapi.wrapper.generic.CafeGeneric;
import com.beanbeanjuice.cafeapi.wrapper.requests.RequestLocation;
import org.junit.jupiter.api.*;

import java.sql.Timestamp;
import java.util.concurrent.ExecutionException;

public class CafeUsersEndpointTests {

    private static CafeAPI cafeAPI;

    @BeforeAll
    @DisplayName("Login to CafeAPI")
    public static void login() {
        cafeAPI = new CafeAPI("beanbeanjuice", System.getenv("API_PASSWORD"), RequestLocation.BETA);
    }

    @BeforeEach
    @DisplayName("Delete User")
    public void deleteUser() throws ExecutionException, InterruptedException {
        // Makes sure the user does not exist beforehand.
        Assertions.assertTrue(cafeAPI.getCafeUsersEndpoint().deleteCafeUser("236654580300120064").get());
    }

    @Test
    @DisplayName("Make Sure User Does Not Exist")
    public void confirmUserAbsence() {
        // Makes sure the user does not exist when trying to get it.
        try {
            cafeAPI.getCafeUsersEndpoint().getCafeUser("236654580300120064").get();
            Assertions.fail();
        } catch (Exception e) {
            Assertions.assertInstanceOf(NotFoundException.class, e.getCause());
        }
    }

    @Test
    @DisplayName("Make Sure User Can Be Created")
    public void createUser() throws ExecutionException, InterruptedException {
        // Creates the user.
        Assertions.assertTrue(cafeAPI.getCafeUsersEndpoint().createCafeUser("236654580300120064").get());
    }

    @Test
    @DisplayName("Test Duplicate User Fail")
    public void testDuplicateUserFail() throws ExecutionException, InterruptedException {
        createUser();

        // Makes sure the user cannot be created twice.
        try {
            cafeAPI.getCafeUsersEndpoint().createCafeUser("236654580300120064").get();
            Assertions.fail();
        } catch (Exception e) {
            Assertions.assertInstanceOf(ConflictException.class, e.getCause());
        }
    }

    @Test
    @DisplayName("Cafe Users Endpoint Test")
    public void testCafeUsersEndpoint() throws ExecutionException, InterruptedException {
        createUser();

        // Makes sure the user exists in the array list.
        Assertions.assertTrue(() -> {
            try {
                for (CafeUser cafeUser : cafeAPI.getCafeUsersEndpoint().getAllCafeUsers().get()) {
                    if (cafeUser.getUserID().equals("236654580300120064")) {
                        return true;
                    }
                }
            } catch (Exception e) {
                Assertions.fail();
            }
            return false;
        });

        // Makes sure the user ID matches the one retrieved.
        Assertions.assertEquals("236654580300120064", cafeAPI.getCafeUsersEndpoint().getCafeUser("236654580300120064").get().getUserID());

        // Makes sure all settings are default when first created.
        Assertions.assertEquals(0, cafeAPI.getCafeUsersEndpoint().getCafeUser("236654580300120064").get().getBeanCoins());
        Assertions.assertNull(cafeAPI.getCafeUsersEndpoint().getCafeUser("236654580300120064").get().getLastServingTime().orElse(null));
        Assertions.assertEquals(0, cafeAPI.getCafeUsersEndpoint().getCafeUser("236654580300120064").get().getOrdersBought());
        Assertions.assertEquals(0, cafeAPI.getCafeUsersEndpoint().getCafeUser("236654580300120064").get().getOrdersReceived());

        // Makes sure the beanCoins can be changed.
        Assertions.assertTrue(cafeAPI.getCafeUsersEndpoint().updateCafeUser("236654580300120064", CafeType.BEAN_COINS, 100.0).get());
        Assertions.assertEquals(100.0, cafeAPI.getCafeUsersEndpoint().getCafeUser("236654580300120064").get().getBeanCoins());

        // Makes sure the timestamp can be changed.
        Timestamp currentTimeStamp = CafeGeneric.parseTimestamp(new Timestamp(System.currentTimeMillis()).toString()).orElse(null);
        Assertions.assertTrue(cafeAPI.getCafeUsersEndpoint().updateCafeUser("236654580300120064", CafeType.LAST_SERVING_TIME, currentTimeStamp).get());
        Assertions.assertEquals(currentTimeStamp, cafeAPI.getCafeUsersEndpoint().getCafeUser("236654580300120064").get().getLastServingTime().orElse(null));

        // Makes sure the timestamp can be changed to null.
        Assertions.assertTrue(cafeAPI.getCafeUsersEndpoint().updateCafeUser("236654580300120064", CafeType.LAST_SERVING_TIME, null).get());
        Assertions.assertNull(cafeAPI.getCafeUsersEndpoint().getCafeUser("236654580300120064").get().getLastServingTime().orElse(null));

        // Makes sure the orders bought can be updated.
        Assertions.assertTrue(cafeAPI.getCafeUsersEndpoint().updateCafeUser("236654580300120064", CafeType.ORDERS_BOUGHT, 10).get());
        Assertions.assertEquals(10, cafeAPI.getCafeUsersEndpoint().getCafeUser("236654580300120064").get().getOrdersBought());

        // Makes sure the orders received can be updated.
        Assertions.assertTrue(cafeAPI.getCafeUsersEndpoint().updateCafeUser("236654580300120064", CafeType.ORDERS_RECEIVED, 15).get());
        Assertions.assertEquals(15, cafeAPI.getCafeUsersEndpoint().getCafeUser("236654580300120064").get().getOrdersReceived());

        // Makes sure a TeaPotException is thrown when trying to update using an invalid value.
        Assertions.assertThrows(TeaPotException.class, () -> cafeAPI.getCafeUsersEndpoint().updateCafeUser("236654580300120064", CafeType.LAST_SERVING_TIME, 100).get());

        // Deletes a cafe user
        Assertions.assertTrue(cafeAPI.getCafeUsersEndpoint().deleteCafeUser("236654580300120064").get());
    }

}
