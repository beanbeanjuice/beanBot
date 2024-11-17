package com.beanbeanjuice.cafeapi.beta;

import com.beanbeanjuice.cafeapi.wrapper.CafeAPI;
import com.beanbeanjuice.cafeapi.wrapper.endpoints.interactions.InteractionType;
import com.beanbeanjuice.cafeapi.wrapper.exception.api.ConflictException;
import com.beanbeanjuice.cafeapi.wrapper.exception.api.NotFoundException;
import com.beanbeanjuice.cafeapi.wrapper.requests.RequestLocation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

public class InteractionsEndpointTests {

    @Test
    @DisplayName("Interactions (SENDERS) Endpoint Test")
    public void testInteractionSendersEndpoint() throws ExecutionException, InterruptedException {
        CafeAPI cafeAPI = new CafeAPI("beanbeanjuice", System.getenv("API_PASSWORD"), RequestLocation.BETA);

        // Makes sure to delete the user beforehand.
        Assertions.assertTrue(cafeAPI.getInteractionsEndpoint().deleteUserInteractionsSent("879761226761109544").get());

        // Makes sure that a NotFoundException is thrown when trying to get the user.
        cafeAPI.getInteractionsEndpoint().getUserInteractionsSent("879761226761109544")
                .thenAcceptAsync((interaction) -> Assertions.fail())
                .exceptionallyAsync((exception) -> {
                    Assertions.assertInstanceOf(NotFoundException.class, exception.getCause());
                    return null;
                })
                .join();

        // Makes sure it is able to create the user.
        Assertions.assertTrue(cafeAPI.getInteractionsEndpoint().createUserInteractionsSent("879761226761109544").get());

        // Makes sure a ConflictException is thrown when the same user is tried to be made again.
        cafeAPI.getInteractionsEndpoint().createUserInteractionsSent("879761226761109544")
                .thenAcceptAsync((interaction) -> Assertions.fail())
                .exceptionallyAsync((exception) -> {
                    Assertions.assertInstanceOf(ConflictException.class, exception.getCause());
                    return null;
                })
                .join();

        int interactionValue = 10;
        for (InteractionType type : InteractionType.values()) {
            Assertions.assertTrue(cafeAPI.getInteractionsEndpoint().updateSpecificUserInteractionSentAmount("879761226761109544", type, interactionValue).get());
            Assertions.assertEquals(interactionValue, cafeAPI.getInteractionsEndpoint().getAllInteractionSenders().get().get("879761226761109544").getInteractionAmount(type));
            interactionValue++;
        }

        // Deletes the user from the database.
        Assertions.assertTrue(cafeAPI.getInteractionsEndpoint().deleteUserInteractionsSent("879761226761109544").get());
    }

    @Test
    @DisplayName("Interactions (RECEIVERS) Endpoint Test")
    public void testInteractionsReceiversEndpoint() throws ExecutionException, InterruptedException {
        CafeAPI cafeAPI = new CafeAPI("beanbeanjuice", System.getenv("API_PASSWORD"), RequestLocation.BETA);

        // Makes sure to delete the user before-hand.
        Assertions.assertTrue(cafeAPI.getInteractionsEndpoint().deleteUserInteractionsReceived("879761226761109544").get());

        // Makes sure a NotFoundException is thrown when trying to search for the user.
        cafeAPI.getInteractionsEndpoint().getUserInteractionsReceived("879761226761109544")
                .thenAcceptAsync((interaction) -> Assertions.fail())
                .exceptionallyAsync((exception) -> {
                    Assertions.assertInstanceOf(NotFoundException.class, exception.getCause());
                    return null;
                })
                .join();

        // Makes sure the user can be created.
        Assertions.assertTrue(cafeAPI.getInteractionsEndpoint().createUserInteractionsReceived("879761226761109544").get());

        // Makes sure a ConflictException is thrown when trying to make the user again.
        cafeAPI.getInteractionsEndpoint().createUserInteractionsReceived("879761226761109544")
                .thenAcceptAsync((interaction) -> Assertions.fail())
                .exceptionallyAsync((exception) -> {
                    Assertions.assertInstanceOf(ConflictException.class, exception.getCause());
                    return null;
                })
                .join();

        int interactionValue = 10;
        for (InteractionType type : InteractionType.values()) {
            Assertions.assertTrue(cafeAPI.getInteractionsEndpoint().updateSpecificUserInteractionReceivedAmount("879761226761109544", type, interactionValue).get());
            Assertions.assertEquals(interactionValue, cafeAPI.getInteractionsEndpoint().getAllInteractionReceivers().get().get("879761226761109544").getInteractionAmount(type));
            interactionValue++;
        }

        // Deletes the user from the database.
        Assertions.assertTrue(cafeAPI.getInteractionsEndpoint().deleteUserInteractionsReceived("879761226761109544").get());
    }

}
