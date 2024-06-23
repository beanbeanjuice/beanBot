package com.beanbeanjuice.cafeapi.wrapper.endpoints.goodbyes;

import com.beanbeanjuice.cafeapi.wrapper.CafeAPI;
import com.beanbeanjuice.cafeapi.wrapper.endpoints.CafeEndpoint;
import com.beanbeanjuice.cafeapi.wrapper.requests.Request;
import com.beanbeanjuice.cafeapi.wrapper.requests.RequestBuilder;
import com.beanbeanjuice.cafeapi.wrapper.requests.RequestRoute;
import com.beanbeanjuice.cafeapi.wrapper.requests.RequestType;
import com.beanbeanjuice.cafeapi.wrapper.exception.api.*;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;

/**
 * A class used for the {@link GoodbyesEndpoint} API.
 */
public class GoodbyesEndpoint extends CafeEndpoint {

    /**
     * Retrieves an {@link ArrayList} of {@link GuildGoodbye} containing all Guild Goodbyes in the {@link CafeAPI CafeAPI}.
     * @return The {@link ArrayList} of {@link GuildGoodbye}.
     * @throws AuthorizationException Thrown when the api key is unauthorized.
     * @throws ResponseException Thrown when there is a generic server-side exception.
     */
    public ArrayList<GuildGoodbye> getAllGuildGoodbyes() throws AuthorizationException, ResponseException {
        ArrayList<GuildGoodbye> guildGoodbyes = new ArrayList<>();

        Request request = RequestBuilder.create(RequestRoute.CAFEBOT, RequestType.GET)
                .setRoute("/goodbyes")
                .setAuthorization(apiKey)
                .build().orElseThrow();

        request.getData().get("goodbyes").forEach((guildGoodbye) -> guildGoodbyes.add(parseGuildGoodbye(guildGoodbye)));

        return guildGoodbyes;
    }

    /**
     * Retrieves a {@link GuildGoodbye} from the {@link CafeAPI CafeAPI}.
     * @param guildID The {@link String guildID} to retrieve the {@link GuildGoodbye} for.
     * @return The {@link GuildGoodbye} retrieved.
     * @throws AuthorizationException Thrown when the API key is invalid.
     * @throws ResponseException Thrown when there is a generic server-side exception.
     * @throws NotFoundException Thrown when the guild ID is not found.
     */
    public GuildGoodbye getGuildGoodbye(final String guildID)
            throws AuthorizationException, ResponseException, NotFoundException {
        Request request = RequestBuilder.create(RequestRoute.CAFEBOT, RequestType.GET)
                .setRoute("/goodbyes/" + guildID)
                .setAuthorization(apiKey)
                .build().orElseThrow();

        JsonNode guildGoodbye = request.getData().get("goodbye");

        return parseGuildGoodbye(guildGoodbye);
    }

    /**
     * Updates a {@link GuildGoodbye} in the {@link CafeAPI CafeAPI}.
     * @param guildGoodbye The new {@link GuildGoodbye}.
     * @return True, if updating the {@link GuildGoodbye} was successful.
     * @throws AuthorizationException Thrown when the API key is invalid.
     * @throws NotFoundException Thrown when the guild ID is not found.
     * @throws ResponseException Thrown when there is a generic server-side exception.
     */
    public Boolean updateGuildGoodbye(final GuildGoodbye guildGoodbye)
            throws AuthorizationException, NotFoundException, ResponseException {
        Request request = RequestBuilder.create(RequestRoute.CAFEBOT, RequestType.PATCH)
                .setRoute("/goodbyes/" + guildGoodbye.getGuildID())
                .addParameter("description", guildGoodbye.getDescription().orElse(null))
                .addParameter("thumbnail_url", guildGoodbye.getThumbnailURL().orElse(null))
                .addParameter("image_url", guildGoodbye.getImageURL().orElse(null))
                .addParameter("message", guildGoodbye.getMessage().orElse(null))
                .setAuthorization(apiKey)
                .build().orElseThrow();

        return request.getStatusCode() == 200;
    }

    /**
     * Creates a new {@link GuildGoodbye} for the {@link CafeAPI CafeAPI}.
     * @param guildGoodbye The new {@link GuildGoodbye} to add.
     * @return True if the {@link GuildGoodbye} was successfully added.
     * @throws AuthorizationException Thrown when the API key is invalid.
     * @throws ConflictException Thrown when the provided guild ID already exists.
     * @throws ResponseException Thrown when there is a generic server-side exception.
     * @throws UndefinedVariableException Thrown when a variable is undefined.
     */
    public Boolean createGuildGoodbye(final GuildGoodbye guildGoodbye)
            throws AuthorizationException, ConflictException, ResponseException, UndefinedVariableException {
        Request request = RequestBuilder.create(RequestRoute.CAFEBOT, RequestType.POST)
                .setRoute("/goodbyes/" + guildGoodbye.getGuildID())
                .addParameter("description", guildGoodbye.getDescription().orElse(null))
                .addParameter("thumbnail_url", guildGoodbye.getThumbnailURL().orElse(null))
                .addParameter("image_url", guildGoodbye.getImageURL().orElse(null))
                .addParameter("message", guildGoodbye.getMessage().orElse(null))
                .setAuthorization(apiKey)
                .build().orElseThrow();

        return request.getStatusCode() == 201;
    }

    /**
     * Deletes a {@link GuildGoodbye} from the {@link CafeAPI CafeAPI}.
     * @param guildID The {@link String} ID of the {@link GuildGoodbye} to delete.
     * @return True if successfully deleted.
     * @throws AuthorizationException Thrown when the API key is invalid.
     * @throws ResponseException Thrown when there is a generic server-side exception.
     */
    public Boolean deleteGuildGoodbye(final String guildID)
            throws AuthorizationException, ResponseException {
        Request request = RequestBuilder.create(RequestRoute.CAFEBOT, RequestType.DELETE)
                .setRoute("/goodbyes/" + guildID)
                .setAuthorization(apiKey)
                .build().orElseThrow();

        return request.getStatusCode() == 200;
    }

    /**
     * Parses a {@link GuildGoodbye} from the {@link JsonNode}.
     * @param node The {@link JsonNode} to parse.
     * @return The parsed {@link GuildGoodbye}.
     */
    private GuildGoodbye parseGuildGoodbye(final JsonNode node) {
        String guildID = node.get("guild_id").asText();

        String description = node.get("description").asText();
        String thumbnailURL = node.get("thumbnail_url").asText();
        String imageURL = node.get("image_url").asText();
        String message = node.get("message").asText();

        if (description.equals("null")) description = null;
        if (thumbnailURL.equals("null")) thumbnailURL = null;
        if (imageURL.equals("null")) imageURL = null;
        if (message.equals("null")) message = null;

        return new GuildGoodbye(
                guildID,
                description,
                thumbnailURL,
                imageURL,
                message
        );
    }

}
