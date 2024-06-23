package com.beanbeanjuice.kawaiiapi.wrapper.requests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleRequestBuilder;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.core5.http.HttpResponse;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link RequestBuilder} used to build {@link Request} objects.
 *
 * @author beanbeanjuice
 * @since 2.0.0
 */
public class RequestBuilder {

    private String apiURL;
    private Consumer<Request> successConsumer;
    private Consumer<Exception> errorConsumer;

    /**
     * Creates a new {@link RequestBuilder}.
     * @param requestRoute The {@link RequestRoute} of the {@link Request}.
     */
    public RequestBuilder(final RequestRoute requestRoute) {
        apiURL = requestRoute.getLink();
    }

    /**
     * Creates a new {@link RequestBuilder}
     * @param requestRoute The {@link RequestRoute} of the {@link Request}.
     * @return The new {@link RequestBuilder}.
     */
    public static RequestBuilder create(final RequestRoute requestRoute) {
        return new RequestBuilder(requestRoute);
    }

    /**
     * Sets the 'type'.
     * @param value The {@link String value} to set the type to.
     * @return The new {@link RequestBuilder}.
     */
    public RequestBuilder setParameter(final String value) {
        apiURL = apiURL.replace("{type}", value);
        return this;
    }

    /**
     * Sets the API key for the {@link RequestBuilder}.
     * @param apiKey The {@link String api key} for the {@link RequestBuilder}.
     * @return The new {@link RequestBuilder}.
     */
    public RequestBuilder setAuthorization(final String apiKey) {
        apiURL = apiURL.replace("{token}", apiKey);
        return this;
    }

    /**
     * Builds and runs the {@link Request}.
     * @return An {@link Optional} containing the {@link Request}.
     */
    public Optional<Request> build() {
        try {
            SimpleHttpRequest httpRequest = SimpleRequestBuilder.get(apiURL).build();
            SimpleHttpResponse httpResponse = (SimpleHttpResponse) get(httpRequest);

            byte[] content = httpResponse.getBody().getBodyBytes();

            Request request = new Request(httpResponse.getCode(), new ObjectMapper().readTree(content));

            if (successConsumer != null) successConsumer.accept(request);

            return Optional.of(request);
        } catch (Exception e) {
            if (errorConsumer != null) errorConsumer.accept(e);
            else Logger.getLogger(RequestBuilder.class.getName()).log(Level.WARNING, "Error queuing request: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Builds and runs the {@link Request} with a provided {@link Consumer<Request> function}.
     * @param consumer The {@link Consumer<Request> function} to run.
     * @return An {@link Optional} containing the {@link Request}.
     */
    public Optional<Request> build(final Consumer<Request> consumer) {
        this.successConsumer = consumer;
        return build();
    }

    /**
     * Builds the {@link Request} asynchronously on a separate {@link Thread}.
     */
    public void buildAsync() {
        Thread thread = new Thread(this::build);
        thread.start();
    }

    /**
     * Builds the {@link Request} asynchronously on a separate {@link Thread}.
     * @param consumer The {@link Consumer} function to run on the {@link Request} once finished.
     */
    public void buildAsync(final Consumer<Request> consumer) {
        this.successConsumer = consumer;
        buildAsync();
    }

    private HttpResponse get(final SimpleHttpRequest request) throws IOException, ExecutionException, InterruptedException {
        CloseableHttpAsyncClient client = HttpAsyncClients.custom().build();
        client.start();

        Future<SimpleHttpResponse> future = client.execute(request, null);
        HttpResponse response = future.get();

        client.close();
        return response;
    }

    /**
     * The function to run upon a successful 200 return code.
     * @param consumer The {@link Consumer} function taking in a {@link Request} to run on.
     */
    public RequestBuilder onSuccess(Consumer<Request> consumer) {
        this.successConsumer = consumer;
        return this;
    }

    /**
     * The function to run upon a failure. This is any code other than 200.
     * @param consumer The {@link Consumer} function taking in a {@link Exception} to run on.
     */
    public RequestBuilder onError(Consumer<Exception> consumer) {
        this.errorConsumer = consumer;
        return this;
    }

}
