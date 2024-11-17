package com.beanbeanjuice.cafeapi.wrapper.endpoints.birthdays;

import com.beanbeanjuice.cafeapi.wrapper.endpoints.CafeEndpoint;
import com.beanbeanjuice.cafeapi.wrapper.requests.Request;
import com.beanbeanjuice.cafeapi.wrapper.requests.RequestBuilder;
import com.beanbeanjuice.cafeapi.wrapper.requests.RequestRoute;
import com.beanbeanjuice.cafeapi.wrapper.requests.RequestType;
import com.beanbeanjuice.cafeapi.wrapper.exception.api.*;
import com.beanbeanjuice.cafeapi.wrapper.utility.Time;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.Year;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class BirthdaysEndpoint extends CafeEndpoint {

    private HashMap<String, Birthday> requestToBirthdayMap(Request request) {
        HashMap<String, Birthday> birthdays = new HashMap<>();

        JsonNode birthdaysNode = request.getData().get("birthdays");

        for (JsonNode birthdayNode : birthdaysNode) {
            String userID = birthdayNode.get("user_id").asText();
            parseBirthday(birthdayNode).ifPresent((birthday -> birthdays.put(userID, birthday)));
        }

        return birthdays;
    }

    public CompletableFuture<HashMap<String, Birthday>> getAllBirthdays() throws AuthorizationException, ResponseException {
        return RequestBuilder.create(RequestRoute.CAFEBOT, RequestType.GET)
                .setRoute("/birthdays")
                .setAuthorization(apiKey)
                .buildAsync()
                .thenApplyAsync(this::requestToBirthdayMap);
    }

    public CompletableFuture<Optional<Birthday>> getUserBirthday(final String userID) {
        return RequestBuilder.create(RequestRoute.CAFEBOT, RequestType.GET)
                .setRoute("/birthdays/" + userID)
                .setAuthorization(apiKey)
                .buildAsync()
                .thenApplyAsync((request) -> parseBirthday(request.getData().get("birthday")));
    }

    public CompletableFuture<Boolean> updateUserBirthday(final String userID, final Birthday birthday) {
        BirthdayMonth month = birthday.getMonth();
        int day = birthday.getDay();

        return RequestBuilder.create(RequestRoute.CAFEBOT, RequestType.PATCH)
                .setRoute("/birthdays/" + userID)
                .addParameter("birth_date", getBirthdayString(month, day))
                .addParameter("time_zone", birthday.getTimeZone().getID())
                .setAuthorization(apiKey)
                .buildAsync()
                .thenApplyAsync((request) -> request.getStatusCode() == 200);
    }

    public CompletableFuture<Boolean> createUserBirthday(final String userID, final Birthday birthday) {
        BirthdayMonth month = birthday.getMonth();
        int day = birthday.getDay();

        return RequestBuilder.create(RequestRoute.CAFEBOT, RequestType.POST)
                .setRoute("/birthdays/" + userID)
                .addParameter("birth_date", getBirthdayString(month, day))
                .addParameter("time_zone", birthday.getTimeZone().getID())
                .setAuthorization(apiKey)
                .buildAsync()
                .thenApplyAsync((request) -> request.getStatusCode() == 201);
    }

    public CompletableFuture<Boolean> removeUserBirthday(final String userID) {
        return RequestBuilder.create(RequestRoute.CAFEBOT, RequestType.DELETE)
                .setRoute("/birthdays/" + userID)
                .setAuthorization(apiKey)
                .buildAsync()
                .thenApplyAsync((request) -> request.getStatusCode() == 200);
    }

    private Optional<Birthday> parseBirthday(final JsonNode birthday) {
        String unformattedDate = birthday.get("birth_date").asText();
        String timeZoneString = birthday.get("time_zone").asText();

        try {
            int month = Integer.parseInt(unformattedDate.split("-")[0]);
            int day = Integer.parseInt(unformattedDate.split("-")[1]);

            return Optional.of(new Birthday(getBirthdayMonth(month), day, timeZoneString));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private BirthdayMonth getBirthdayMonth(final int index) {
        for (BirthdayMonth month : BirthdayMonth.values()) {
            if (month.getMonthNumber() == index)
                return month;
        }

        return BirthdayMonth.ERROR;
    }

    private String parseNumber(final int number) {  // TODO: There MUST be a better way to do this.
        if (number <= 9) return "0" + number;
        return String.valueOf(number);
    }

    private String getBirthdayString(final BirthdayMonth month, final int day) {
        return parseNumber(month.getMonthNumber()) + "-" + parseNumber(day);
    }

}
