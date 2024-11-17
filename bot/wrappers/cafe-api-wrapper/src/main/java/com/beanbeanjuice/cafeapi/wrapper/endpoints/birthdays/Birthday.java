package com.beanbeanjuice.cafeapi.wrapper.endpoints.birthdays;

import com.beanbeanjuice.cafeapi.wrapper.exception.program.BirthdayOverfillException;
import com.beanbeanjuice.cafeapi.wrapper.exception.program.InvalidTimeZoneException;
import com.beanbeanjuice.cafeapi.wrapper.utility.Time;
import lombok.Getter;

import java.text.ParseException;
import java.time.Year;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class used to house {@link Birthday}.
 *
 * @author beanbeanjuice
 */
public class Birthday {

    @Getter private final BirthdayMonth month;
    @Getter private final int day;
    @Getter private final TimeZone timeZone;

    public Birthday(final BirthdayMonth month, final int day, final String timeZone) {
        this.month = month;
        this.day = day;

        if (month == BirthdayMonth.ERROR)
            throw new BirthdayOverfillException("The month you specified is invalid.");

        if (month.getDaysInMonth() < day)
            throw new BirthdayOverfillException("You specified more days than there are in this month!");

        if (!Time.isValidTimeZone(timeZone))
            throw new InvalidTimeZoneException("The timezone specified is not allowed!");

        this.timeZone = TimeZone.getTimeZone(timeZone);
    }

    public Date getDate() throws ParseException {
        int year = Year.now(timeZone.toZoneId()).getValue();
        String dateString = convertToBirthdayDateString(month, day, year);
        return Time.getFullDate(dateString, timeZone);
    }

    public boolean isBirthday() {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            Date now = calendar.getTime();

            return this.getDate().getTime() == now.getTime();
        } catch (ParseException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Error Checking if it is their birthday.", e);
            return false;
        }
    }

    private static String convertToBirthdayDateString(final BirthdayMonth month, final int day, final int year) {
        return String.format("%s-%s-%d", month.getMonthNumber(), day, year);
    }

}
