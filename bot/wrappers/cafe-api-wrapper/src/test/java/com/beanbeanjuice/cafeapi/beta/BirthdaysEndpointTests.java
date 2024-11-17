package com.beanbeanjuice.cafeapi.beta;

import com.beanbeanjuice.cafeapi.wrapper.CafeAPI;
import com.beanbeanjuice.cafeapi.wrapper.endpoints.birthdays.Birthday;
import com.beanbeanjuice.cafeapi.wrapper.endpoints.birthdays.BirthdayMonth;
import com.beanbeanjuice.cafeapi.wrapper.exception.api.ConflictException;
import com.beanbeanjuice.cafeapi.wrapper.exception.api.NotFoundException;
import com.beanbeanjuice.cafeapi.wrapper.exception.api.TeaPotException;
import com.beanbeanjuice.cafeapi.wrapper.exception.program.BirthdayOverfillException;
import com.beanbeanjuice.cafeapi.wrapper.exception.program.InvalidTimeZoneException;
import com.beanbeanjuice.cafeapi.wrapper.requests.RequestLocation;
import org.junit.jupiter.api.*;

import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

public class BirthdaysEndpointTests {

    private static CafeAPI cafeAPI;

    @BeforeAll
    @DisplayName("Login to CafeAPI")
    public static void login() {
        cafeAPI = new CafeAPI("beanbeanjuice", System.getenv("API_PASSWORD"), RequestLocation.BETA);
    }

    @BeforeEach
    public void deleteUserBirthday() throws ExecutionException, InterruptedException {
        Assertions.assertTrue(cafeAPI.getBirthdaysEndpoint().removeUserBirthday("178272524533104642").get());
    }

    @Test
    @DisplayName("Make Sure User Birthday Does Not Already Exist")
    public void testIfBirthdayDoesNotExist() {
        try {
            cafeAPI.getBirthdaysEndpoint().getUserBirthday("178272524533104642").get();
            Assertions.fail();
        } catch (Exception e) {
            Assertions.assertInstanceOf(NotFoundException.class, e.getCause());
        }
    }

    @Test
    @DisplayName("Test Birthday Creation")
    public void testBirthdayCreation() throws ExecutionException, InterruptedException {
        Assertions.assertTrue(cafeAPI.getBirthdaysEndpoint().createUserBirthday("178272524533104642", new Birthday(BirthdayMonth.DECEMBER, 31, "EST")).get());
    }

    @Test
    @DisplayName("Test Birthday Duplication")
    public void testBirthdayDuplication() throws ExecutionException, InterruptedException {
        testBirthdayCreation();

        try {
            cafeAPI.getBirthdaysEndpoint().createUserBirthday("178272524533104642", new Birthday(BirthdayMonth.DECEMBER, 20, "EST")).get();
            Assertions.fail();
        } catch (Exception e) {
            Assertions.assertInstanceOf(ConflictException.class, e.getCause());
        }
    }

    @Test
    @DisplayName("Make Sure Created Birthday Matches Expected Value")
    public void testExpectedValue() throws ExecutionException, InterruptedException {
        testBirthdayCreation();

        Optional<Birthday> optionalBirthday = cafeAPI.getBirthdaysEndpoint().getUserBirthday("178272524533104642").get();

        Assertions.assertTrue(optionalBirthday.isPresent());
        Birthday birthday = optionalBirthday.get();

        Assertions.assertEquals(birthday.getMonth(), BirthdayMonth.DECEMBER);
        Assertions.assertEquals(birthday.getDay(), 31);
        Assertions.assertEquals(birthday.getTimeZone(), TimeZone.getTimeZone("EST"));
    }

    @Test
    @DisplayName("Invalid Parameters Test")
    public void testInvalidParameters() throws ExecutionException, InterruptedException {
        // Makes sure the proper exception is thrown when there are more days than in the month.
        Assertions.assertThrows(BirthdayOverfillException.class, () -> cafeAPI.getBirthdaysEndpoint().updateUserBirthday("178272524533104642", new Birthday(BirthdayMonth.FEBRUARY, 30, "EST")).get());

        // Makes sure the proper exception is thrown when the incorrect month is set.
        Assertions.assertThrows(TeaPotException.class, () -> cafeAPI.getBirthdaysEndpoint().updateUserBirthday("178272524533104642", new Birthday(BirthdayMonth.ERROR, 15, "EST")).get());

        // Makes sure the proper exception is thrown when the incorrect timezone is set.
        Assertions.assertThrows(InvalidTimeZoneException.class, () -> cafeAPI.getBirthdaysEndpoint().updateUserBirthday("178272524533104642", new Birthday(BirthdayMonth.FEBRUARY, 15, "BURGER")).get());
    }

    @Test
    @DisplayName("Confirm Birthday Can Change")
    public void testBirthdayChange() throws ExecutionException, InterruptedException {
        testBirthdayCreation();

        // Makes sure the birthday can be changed.
        Assertions.assertTrue(cafeAPI.getBirthdaysEndpoint().updateUserBirthday("178272524533104642", new Birthday(BirthdayMonth.FEBRUARY, 29, "UTC")).get());
    }

    @Test
    @DisplayName("Confirm Changed Expected Values")
    public void confirmChangedValues() throws ExecutionException, InterruptedException {
        testBirthdayChange();

        Optional<Birthday> optionalBirthday = cafeAPI.getBirthdaysEndpoint().getUserBirthday("178272524533104642").get();

        Assertions.assertTrue(optionalBirthday.isPresent());
        Birthday birthday = optionalBirthday.get();

        Assertions.assertEquals(birthday.getMonth(), BirthdayMonth.FEBRUARY);
        Assertions.assertEquals(birthday.getDay(), 29);
        Assertions.assertEquals(birthday.getTimeZone(), TimeZone.getTimeZone("UTC"));
    }

}
