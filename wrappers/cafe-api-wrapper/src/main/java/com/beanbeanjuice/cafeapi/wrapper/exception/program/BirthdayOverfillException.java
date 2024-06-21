package com.beanbeanjuice.cafeapi.wrapper.exception.program;

import com.beanbeanjuice.cafeapi.wrapper.endpoints.birthdays.BirthdayMonth;

/**
 * A {@link RuntimeException} that deals with incorrect days in a {@link BirthdayMonth BirthdayMonth}.
 *
 * @author beanbeanjuice
 */
public class BirthdayOverfillException extends RuntimeException {

    /**
     * Creates a new {@link BirthdayOverfillException}.
     * @param message The message to send to the {@link RuntimeException}.
     */
    public BirthdayOverfillException(final String message) {
        super(message);
    }

    /**
     * Creates a new {@link BirthdayOverfillException}.
     */
    public BirthdayOverfillException() {
        super("More days than there are days in the month.");
    }

}
