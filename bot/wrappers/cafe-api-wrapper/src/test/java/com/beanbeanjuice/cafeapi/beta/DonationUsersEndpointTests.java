package com.beanbeanjuice.cafeapi.beta;

import com.beanbeanjuice.cafeapi.wrapper.CafeAPI;
import com.beanbeanjuice.cafeapi.wrapper.exception.api.ConflictException;
import com.beanbeanjuice.cafeapi.wrapper.exception.api.NotFoundException;
import com.beanbeanjuice.cafeapi.wrapper.generic.CafeGeneric;
import com.beanbeanjuice.cafeapi.wrapper.requests.RequestLocation;
import org.junit.jupiter.api.*;

import java.sql.Timestamp;
import java.util.concurrent.ExecutionException;

public class DonationUsersEndpointTests {

    private static CafeAPI cafeAPI;

    @BeforeAll
    public static void login() {
        cafeAPI = new CafeAPI("beanbeanjuice", System.getenv("API_PASSWORD"), RequestLocation.BETA);
    }

    @BeforeEach
    public void deleteUser() throws ExecutionException, InterruptedException {
        Assertions.assertTrue(cafeAPI.getDonationUsersEndpoint().deleteDonationUser("738590591767543921").get());
    }

    @Test
    @DisplayName("Make Sure Donation User Does Not Exist")
    public void testAbsence() {
        try {
            cafeAPI.getDonationUsersEndpoint().getUserDonationTime("738590591767543921").get();
            Assertions.fail();
        } catch (Exception e) {
            Assertions.assertInstanceOf(NotFoundException.class, e.getCause());
        }
    }

    @Test
    @DisplayName("Make Sure Donation User Can Be Created")
    public void testCreateDonationUser() throws ExecutionException, InterruptedException {
        Timestamp currentTimestamp = CafeGeneric.parseTimestamp((new Timestamp(System.currentTimeMillis())).toString()).orElse(null);
        Assertions.assertTrue(cafeAPI.getDonationUsersEndpoint().addDonationUser("738590591767543921", currentTimestamp).get());
    }

    @Test
    @DisplayName("Make Sure Donation User Time Exists")
    public void testCreateDonationUserTime() throws ExecutionException, InterruptedException {
        Timestamp currentTimestamp = CafeGeneric.parseTimestamp((new Timestamp(System.currentTimeMillis())).toString()).orElse(null);
        Assertions.assertTrue(cafeAPI.getDonationUsersEndpoint().addDonationUser("738590591767543921", currentTimestamp).get());

        try {
            cafeAPI.getDonationUsersEndpoint().addDonationUser("738590591767543921", currentTimestamp).get();
            Assertions.fail();
        } catch (Exception e) {
            Assertions.assertInstanceOf(ConflictException.class, e.getCause());
        }
        Assertions.assertEquals(currentTimestamp, cafeAPI.getDonationUsersEndpoint().getAllUserDonationTimes().get().get("738590591767543921"));
        Assertions.assertEquals(currentTimestamp, cafeAPI.getDonationUsersEndpoint().getUserDonationTime("738590591767543921").get().orElse(null));
    }

    @Test
    @DisplayName("Confirm Exception After Deletion")
    public void testDonationUsersEndpoint() {
        try {
            cafeAPI.getDonationUsersEndpoint().getUserDonationTime("738590591767543921").get();
            Assertions.fail();
        } catch (Exception e) {
            Assertions.assertInstanceOf(NotFoundException.class, e.getCause());
        }
    }

}
