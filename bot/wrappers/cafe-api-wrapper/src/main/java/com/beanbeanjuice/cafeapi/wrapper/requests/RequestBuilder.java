package com.beanbeanjuice.cafeapi.wrapper.requests;

import com.beanbeanjuice.cafeapi.wrapper.CafeAPI;
import com.beanbeanjuice.cafeapi.wrapper.exception.api.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.net.URIBuilder;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link RequestBuilder} used to build {@link Request} objects.
 *
 * @author beanbeanjuice
 */
public class RequestBuilder {

    private final RequestType requestType;
    private String route;
    private final HashMap<String, String> parameters;

    private final String apiURL;
    private String apiKey;

    /**
     * Creates a new {@link RequestBuilder}.
     * @param requestType The {@link RequestType type} of {@link Request}.
     * @param requestRoute The {@link RequestRoute} of the {@link Request}.
     */
    private RequestBuilder(final RequestRoute requestRoute, final RequestType requestType) {
        this.requestType = requestType;
        parameters = new HashMap<>();

        apiURL = CafeAPI.getRequestLocation().getURL() + requestRoute.getRoute();
    }

    public static RequestBuilder create(final RequestRoute requestRoute, final RequestType requestType) {
        return new RequestBuilder(requestRoute, requestType);
    }

    /**
     * Set's the API route for the {@link RequestBuilder}.
     * @param route The {@link String route} for the {@link RequestBuilder}.
     * @return The new {@link RequestBuilder}.
     */
    public RequestBuilder setRoute(final String route) {
        this.route = route;
        return this;
    }

    /**
     * Adds a parameter to the {@link RequestBuilder}.
     * @param key The {@link String key} for the {@link RequestBuilder}.
     * @param value The {@link String value} for the {@link String key} in the {@link RequestBuilder}.
     * @return The new {@link RequestBuilder}.
     */
    public RequestBuilder addParameter(final String key, @Nullable final String value) {
        parameters.put(key, value);
        return this;
    }

    /**
     * Sets the API key for the {@link RequestBuilder}.
     * @param apiKey The {@link String api key} for the {@link RequestBuilder}.
     * @return The new {@link RequestBuilder}.
     */
    public RequestBuilder setAuthorization(final String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    // TODO: Change this to private.
    public Request build() {
        try {
            URIBuilder uriBuilder = new URIBuilder(apiURL + route);
            parameters.forEach(uriBuilder::setParameter);
            SimpleHttpRequest httpRequest = requestType.getRequest(uriBuilder.build().toString());
            SimpleHttpResponse response = (SimpleHttpResponse) get(httpRequest);

            byte[] content = response.getBody().getBodyBytes();

            int statusCode = response.getCode();

            Request request = new Request(response.getCode(), new ObjectMapper().readTree(content));

            // Catching Status Codes
            switch (statusCode) {
                case 400 -> throw new UndefinedVariableException(request);
                case 401 -> throw new AuthorizationException(request);
                case 404 -> throw new NotFoundException(request);
                case 409 -> throw new ConflictException(request);
                case 418 -> throw new TeaPotException(request);
                case 500 -> throw new ResponseException(request);
            }

            return request;
        } catch (URISyntaxException | ExecutionException | InterruptedException | IOException e) {
            Logger.getLogger(RequestBuilder.class.getName()).log(Level.WARNING, "Error queuing request: " + e.getMessage());
            throw new CompletionException(e);
        }

    }

    /**
     * Builds the {@link Request} asynchronously on a separate {@link Thread}.
     */
    public CompletableFuture<Request> buildAsync() {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        return CompletableFuture.supplyAsync(this::build, exec);
    }

    private HttpResponse get(final SimpleHttpRequest request) throws ExecutionException, InterruptedException, IOException {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        CloseableHttpAsyncClient client = HttpAsyncClients.custom().build();
        client.start();

        Future<SimpleHttpResponse> future = client.execute(request, null);
        HttpResponse response = future.get();

        client.close();
        return response;
    }

    /**
     * @return The {@link String route} for the {@link RequestBuilder}.
     */
    public Optional<String> getRoute() {
        return Optional.ofNullable(route);
    }

}
