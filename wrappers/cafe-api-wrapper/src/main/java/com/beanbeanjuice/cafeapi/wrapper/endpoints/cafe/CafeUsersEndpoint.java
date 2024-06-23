package com.beanbeanjuice.cafeapi.wrapper.endpoints.cafe;

import com.beanbeanjuice.cafeapi.wrapper.CafeAPI;
import com.beanbeanjuice.cafeapi.wrapper.endpoints.CafeEndpoint;
import com.beanbeanjuice.cafeapi.wrapper.generic.CafeGeneric;
import com.beanbeanjuice.cafeapi.wrapper.requests.Request;
import com.beanbeanjuice.cafeapi.wrapper.requests.RequestBuilder;
import com.beanbeanjuice.cafeapi.wrapper.requests.RequestRoute;
import com.beanbeanjuice.cafeapi.wrapper.requests.RequestType;
import com.beanbeanjuice.cafeapi.wrapper.exception.api.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * A class used for {@link CafeUser} requests to the {@link CafeAPI CafeAPI}.
 *
 * @author beanbeanjuice
 */
public class CafeUsersEndpoint extends CafeEndpoint {

    /**
     * Retrieves all {@link CafeUser} from the {@link CafeAPI CafeAPI}.
     * @return An {@link ArrayList} of {@link CafeUser}.
     * @throws AuthorizationException Thrown when the {@link String apiKey} is invalid.
     * @throws ResponseException Thrown when there is a generic server-side {@link CafeException}.
     */
    public ArrayList<CafeUser> getAllCafeUsers()
    throws AuthorizationException, ResponseException {
        ArrayList<CafeUser> cafeUsers = new ArrayList<>();

        Request request = RequestBuilder.create(RequestRoute.CAFEBOT, RequestType.GET)
                .setRoute("/cafe/users")
                .setAuthorization(apiKey)
                .build().orElseThrow();

        request.getData().get("users").forEach((user) -> cafeUsers.add(parseCafeUser(user)));

        return cafeUsers;
    }

    /**
     * Retrieves a specified {@link CafeUser}.
     * @param userID The {@link String userID} of the {@link CafeUser}.
     * @return The specified {@link CafeUser}.
     * @throws AuthorizationException Thrown when the {@link String apiKey} is invalid.
     * @throws ResponseException Thrown when there is a generic server-side {@link CafeException}.
     * @throws NotFoundException Thrown when the {@link CafeUser} does not exist for the specified {@link String userID}.
     */
    public CafeUser getCafeUser(final String userID)
    throws AuthorizationException, ResponseException, NotFoundException {
        Request request = RequestBuilder.create(RequestRoute.CAFEBOT, RequestType.GET)
                .setRoute("/cafe/users/" + userID)
                .setAuthorization(apiKey)
                .build().orElseThrow();

        return parseCafeUser(request.getData().get("cafe_user"));
    }

    /**
     * Updates the {@link CafeUser} for the specified {@link String userID}.
     * @param userID The specified {@link String userID}.
     * @param type The specified {@link CafeType type}.
     * @param value The {@link Object value} of the specified {@link CafeType type}.
     * @return True, if the {@link CafeUser} was updated successfully.
     * @throws AuthorizationException Thrown when the {@link String apiKey} is invalid.
     * @throws ResponseException Thrown when there is a generic server-side {@link CafeException}.
     * @throws NotFoundException Thrown when a {@link CafeUser} does not exist for the specified {@link String userID}.
     * @throws TeaPotException Thrown when the {@link Object value} entered is not compatible with the specified {@link CafeType type}.
     * @throws UndefinedVariableException Thrown when a variable is undefined.
     */
    public boolean updateCafeUser(final String userID, final CafeType type, @Nullable final Object value)
    throws AuthorizationException, ResponseException, NotFoundException, TeaPotException, UndefinedVariableException {

        if (!(type.equals(CafeType.LAST_SERVING_TIME) && value == null)) {
            switch (type) {
                case BEAN_COINS -> {
                    if (!(value instanceof Double))
                        throw new TeaPotException("Type Specified Must be a Double");
                }

                case LAST_SERVING_TIME -> {
                    if (!(value instanceof Timestamp))
                        throw new TeaPotException("Type Specified Must be a Timestamp");
                }

                case ORDERS_BOUGHT, ORDERS_RECEIVED -> {
                    if (!(value instanceof Integer))
                        throw new TeaPotException("Type Specified Must be an Integer");
                }
            }
        }

        RequestBuilder requestBuilder = RequestBuilder.create(RequestRoute.CAFEBOT, RequestType.PATCH)
                .setRoute("/cafe/users/" + userID)
                .addParameter("type", type.getType())
                .setAuthorization(apiKey);

        if (value == null) requestBuilder.addParameter("value", "null");
        else requestBuilder.addParameter("value", value.toString());

        Request request = requestBuilder.build().orElseThrow();

        return request.getStatusCode() == 200;
    }

    /**
     * Creates a new {@link CafeUser} for the specified {@link String userID}.
     * @param userID The specified {@link String userID}.
     * @return True, if the {@link CafeUser} was successfully created.
     * @throws AuthorizationException Thrown when the {@link String apiKey} is invalid.
     * @throws ResponseException Thrown when there is a generic server-side {@link CafeException}.
     * @throws ConflictException Thrown when the {@link CafeUser} already exists for the specified {@link String userID}.
     */
    public boolean createCafeUser(final String userID)
    throws AuthorizationException, ResponseException, ConflictException {
        Request request = RequestBuilder.create(RequestRoute.CAFEBOT, RequestType.POST)
                .setRoute("/cafe/users/" + userID)
                .setAuthorization(apiKey)
                .build().orElseThrow();

        return request.getStatusCode() == 201;
    }

    /**
     * Deletes a specified {@link CafeUser}.
     * @param userID The specified {@link String userID}.
     * @return True, if the {@link CafeUser} was successfully deleted.
     * @throws AuthorizationException Thrown when the {@link String apiKey} is invalid.
     * @throws ResponseException Thrown when there is a generic server-side {@link CafeException}.
     */
    public boolean deleteCafeUser(String userID)
    throws AuthorizationException, ResponseException {
        Request request = RequestBuilder.create(RequestRoute.CAFEBOT, RequestType.DELETE)
                .setRoute("/cafe/users/" + userID)
                .setAuthorization(apiKey)
                .build().orElseThrow();

        return request.getStatusCode() == 200;
    }

    /**
     * Parses a {@link JsonNode} for its {@link CafeUser}.
     * @param node The {@link JsonNode node} to parse.
     * @return The parsed {@link CafeUser}.
     */
    private CafeUser parseCafeUser(JsonNode node) {
        String userID = node.get("user_id").asText();
        Double beanCoins = node.get("bean_coins").asDouble();
        Timestamp timestamp = CafeGeneric.parseTimestampFromAPI(node.get("last_serving_time").asText()).orElse(null);
        Integer ordersBought = node.get("orders_bought").asInt();
        Integer ordersReceived = node.get("orders_received").asInt();

        return new CafeUser(userID, beanCoins, timestamp, ordersBought, ordersReceived);
    }

}
