package com.beanbeanjuice.cafeapi.beta;

import com.beanbeanjuice.cafeapi.wrapper.CafeAPI;
import com.beanbeanjuice.cafeapi.wrapper.cafebot.polls.Poll;
import com.beanbeanjuice.cafeapi.wrapper.exception.api.ConflictException;
import com.beanbeanjuice.cafeapi.wrapper.generic.CafeGeneric;
import com.beanbeanjuice.cafeapi.wrapper.requests.RequestLocation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.ArrayList;

public class PollTest {

    @Test
    @DisplayName("Test Polls API")
    public void pollsAPITest() {
        CafeAPI cafeAPI = new CafeAPI("beanbeanjuice", System.getenv("API_PASSWORD"), RequestLocation.BETA);

        long currentTime = System.currentTimeMillis();
        Timestamp currentTimestamp = CafeGeneric.parseTimestamp(new Timestamp(currentTime).toString()).orElse(null);

        // Makes sure the poll doesn't exist before starting the test.
        Assertions.assertTrue(() -> cafeAPI.POLL.deletePoll("798830792938881024", "879519824424890438"));

        // Makes sure that the wrapper is able to create the poll.
        Assertions.assertTrue(() -> cafeAPI.POLL.createPoll("798830792938881024", new Poll("879519824424890438", currentTimestamp)));

        // Makes sure that a ConflictException is thrown when the same poll is attempted to be created twice.
        Assertions.assertThrows(ConflictException.class, () -> {
            cafeAPI.POLL.createPoll("798830792938881024", new Poll("879519824424890438", currentTimestamp));
        });

        // Makes sure that the ending time retrieved from the API is the same as the one entered.
        Assertions.assertTrue(() -> {
            ArrayList<Poll> polls = cafeAPI.POLL.getAllPolls().get("798830792938881024");

            for (Poll poll : polls) {
                if (poll.getEndingTime().equals(currentTimestamp)) {
                    return true;
                }
            }

            return false;
        });

        // Makes sure that the message ID retrieved from the API is the same as the one entered.
        Assertions.assertTrue(() -> {
            ArrayList<Poll> polls = cafeAPI.POLL.getGuildPolls("798830792938881024");

            for (Poll poll : polls) {
                if (poll.getMessageID().equals("879519824424890438")) {
                    return true;
                }
            }

            return false;
        });

        // Makes sure a poll is able to be deleted from the API.
        Assertions.assertTrue(() -> cafeAPI.POLL.deletePoll("798830792938881024", "879519824424890438"));
    }

}
