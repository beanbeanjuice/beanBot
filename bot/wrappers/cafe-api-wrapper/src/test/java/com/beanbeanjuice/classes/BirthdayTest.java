package com.beanbeanjuice.classes;

import com.beanbeanjuice.cafeapi.wrapper.endpoints.birthdays.Birthday;
import com.beanbeanjuice.cafeapi.wrapper.endpoints.birthdays.BirthdayMonth;
import com.beanbeanjuice.cafeapi.wrapper.exception.program.BirthdayOverfillException;
import com.beanbeanjuice.cafeapi.wrapper.exception.program.InvalidTimeZoneException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.time.Year;
import java.util.Calendar;
import java.util.TimeZone;

public class BirthdayTest {

    @Test
    @DisplayName("Birthday Class Test")
    public void testBirthdayException() {
        Assertions.assertThrows(InvalidTimeZoneException.class, () -> {
           new Birthday(BirthdayMonth.FEBRUARY, 25, "burger");
        });

        Assertions.assertThrows(BirthdayOverfillException.class, () -> {
            new Birthday(BirthdayMonth.FEBRUARY, 30, "EST");
        });

        Assertions.assertDoesNotThrow(() -> {
            new Birthday(BirthdayMonth.JANUARY, 1, "UTC");
        });
    }

    @Test
    @DisplayName("Test Birthday Format")
    public void testBirthdayFormat() throws ParseException {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        int birthday1Year = Year.now(TimeZone.getTimeZone("EST").toZoneId()).getValue();
        Birthday birthday1 = new Birthday(BirthdayMonth.JANUARY, 1, "EST");
        Assertions.assertEquals(String.format("Mon Jan 01 05:00:00 UTC %d", birthday1Year), birthday1.getDate().toString());

        int birthday2Year = Year.now(TimeZone.getTimeZone("PST").toZoneId()).getValue();
        Birthday birthday2 = new Birthday(BirthdayMonth.JANUARY, 1, "PST");
        Assertions.assertEquals(String.format("Mon Jan 01 08:00:00 UTC %d", birthday2Year), birthday2.getDate().toString());

        Assertions.assertTrue(birthday1.getDate().before(birthday2.getDate()));
        Assertions.assertFalse(birthday1.getDate().after(birthday2.getDate()));
    }

    @Test
    @DisplayName("Test Not Birthday")
    public void testNotBirthday() {
        Calendar cal = Calendar.getInstance();

        BirthdayMonth month = BirthdayMonth.values()[cal.get(Calendar.MONTH)];
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

        Birthday birthday1 = new Birthday(month, dayOfMonth + 2, "EST");
        Assertions.assertFalse(birthday1.isBirthday());
    }

}
