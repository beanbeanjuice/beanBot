package com.beanbeanjuice.cafeapi.wrapper.endpoints.raffles;

import com.beanbeanjuice.cafeapi.wrapper.CafeAPI;
import lombok.Getter;

import java.sql.Timestamp;

/**
 * A class used to hold {@link RafflesEndpoint} retrieved from the {@link CafeAPI CafeAPI}.
 *
 * @author beanbeanjuice
 */
public class Raffle {

    @Getter private final String messageID;
    @Getter private final Timestamp endingTime;
    @Getter private final int winnerAmount;

    /**
     * Creates a new {@link Raffle} object.
     * @param messageID The {@link String messageID} of the {@link Raffle}.
     * @param endingTime The {@link Timestamp endingTime} of the {@link Raffle}. UTC timezone.
     * @param winnerAmount The {@link Integer winnerAmount} of the {@link Raffle}.
     */
    public Raffle(final String messageID, final Timestamp endingTime, final int winnerAmount) {
        this.messageID = messageID;
        this.endingTime = endingTime;
        this.winnerAmount = winnerAmount;
    }

}
