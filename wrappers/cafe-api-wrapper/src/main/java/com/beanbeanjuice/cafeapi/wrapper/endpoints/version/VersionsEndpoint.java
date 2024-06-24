package com.beanbeanjuice.cafeapi.wrapper.endpoints.version;

import com.beanbeanjuice.cafeapi.wrapper.CafeAPI;
import com.beanbeanjuice.cafeapi.wrapper.endpoints.CafeEndpoint;
import com.beanbeanjuice.cafeapi.wrapper.requests.Request;
import com.beanbeanjuice.cafeapi.wrapper.requests.RequestBuilder;
import com.beanbeanjuice.cafeapi.wrapper.requests.RequestRoute;
import com.beanbeanjuice.cafeapi.wrapper.requests.RequestType;
import com.beanbeanjuice.cafeapi.wrapper.exception.api.AuthorizationException;
import com.beanbeanjuice.cafeapi.wrapper.exception.api.ResponseException;
import com.beanbeanjuice.cafeapi.wrapper.exception.api.TeaPotException;
import com.beanbeanjuice.cafeapi.wrapper.exception.api.UndefinedVariableException;
import com.beanbeanjuice.cafeapi.wrapper.exception.api.CafeException;

/**
 * A class used for handling CafeBot {@link VersionsEndpoint} in the {@link CafeAPI CafeAPI}.
 *
 * @author beanbeanjuice
 */
public class VersionsEndpoint extends CafeEndpoint {

    /**
     * Retrieves the current {@link String botVersion}.
     * @return The current {@link String botVersion} from the {@link CafeAPI CafeAPI}.
     * @throws AuthorizationException Thrown when the {@link String apiKey} is invalid.
     * @throws ResponseException Thrown when there is a generic server-side {@link CafeException CafeException}.
     */
    public String getCurrentCafeBotVersion()
    throws AuthorizationException, ResponseException {
        Request request = RequestBuilder.create(RequestRoute.CAFEBOT, RequestType.GET)
                .setRoute("/cafeBot")
                .setAuthorization(apiKey)
                .build().orElseThrow();

        return  request.getData().get("bot_information").get("version").asText();
    }

    /**
     * Updates the current {@link String botVersion} in the {@link CafeAPI CafeAPI}.
     * @param versionNumber The {@link String versionNumber} to update it to.
     * @return True, if the {@link String versionNumber} was successfully updated.
     * @throws AuthorizationException Thrown when the {@link String apiKey} is invalid.
     * @throws ResponseException Thrown when there is a generic server-side {@link CafeException CafeException}.
     * @throws UndefinedVariableException Thrown when a variable is undefined.
     * @throws TeaPotException Thrown when you forget to add "v" to the beginning of the version number.
     */
    public boolean updateCurrentCafeBotVersion(final String versionNumber)
    throws AuthorizationException, ResponseException, UndefinedVariableException, TeaPotException {
        if (!versionNumber.startsWith("v")) throw new TeaPotException("Version Number Must Start with 'v'.");

        Request request = RequestBuilder.create(RequestRoute.CAFEBOT, RequestType.PATCH)
                .setRoute("/cafeBot")
                .addParameter("version", versionNumber)
                .setAuthorization(apiKey)
                .build().orElseThrow();

        return request.getStatusCode() == 200;
    }

}