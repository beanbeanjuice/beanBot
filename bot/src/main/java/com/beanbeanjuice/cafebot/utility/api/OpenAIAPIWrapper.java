package com.beanbeanjuice.cafebot.utility.api;

import com.beanbeanjuice.cafebot.CafeBot;
import com.beanbeanjuice.cafebot.utility.listeners.ai.PreviousMessage;
import com.beanbeanjuice.cafebot.utility.logging.LogLevel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.net.URIBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RequiredArgsConstructor
public class OpenAIAPIWrapper {

    private final CafeBot bot;
    private final String authorizationKey;
    private final String assistantID;
    private final Map<String, Map<String, Queue<PreviousMessage>>> previousMessageMap;

    private final String URL = "https://api.openai.com/v1";

    private final Map<String, String> aiThreads = new HashMap<>();
    private Map<String, String> headers;

    public void setHeaders() {
        this.headers = new HashMap<>() {{
            put("Authorization", "Bearer " + authorizationKey);
            put("OpenAI-Beta", "assistants=v2");
            put("Content-Type", "application/json");
            put("User-Agent", "cafeBot/4.0.0");
        }};
    }

    private CompletableFuture<String> getThread(final String guildID) throws URISyntaxException {
        if (aiThreads.containsKey(guildID)) return CompletableFuture.supplyAsync(() -> aiThreads.get(guildID));

        return createThread().thenApplyAsync((threadID) -> {
            aiThreads.put(guildID, threadID);
            return threadID;
        });
    }

    private CompletableFuture<String> createThread() throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(String.format("%s/threads", URL));
        SimpleHttpRequest httpRequest = new SimpleHttpRequest(Method.POST, uriBuilder.build());

        return CompletableFuture.supplyAsync(() -> {
            SimpleHttpResponse httpResponse = null;
            try {
                httpResponse = (SimpleHttpResponse) this.get(httpRequest);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            byte[] bodyBytes = httpResponse.getBodyBytes();

            JsonNode body = null;
            try {
                body = new ObjectMapper().readTree(bodyBytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            String newThreadID = body.get("id").asText();
            bot.getLogger().log(OpenAIAPIWrapper.class, LogLevel.DEBUG, "Created AI Thread: " + newThreadID, true, false);

            return newThreadID;
        });
    }

    private CompletableFuture<Boolean> deleteThread(final String threadID) throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(String.format("%s/threads/%s", URL, threadID));
        SimpleHttpRequest httpRequest = new SimpleHttpRequest(Method.DELETE, uriBuilder.build());

        return CompletableFuture.supplyAsync(() -> {
            SimpleHttpResponse httpResponse = null;
            try {
                httpResponse = (SimpleHttpResponse) this.get(httpRequest);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            int status = httpResponse.getCode();
            return status == HttpStatus.SC_OK;
        });
    }

    private CompletableFuture<String> createRun(final String guildID, final String channelID, final String threadID) throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(String.format("%s/threads/%s/runs", URL, threadID));
        SimpleHttpRequest httpRequest = new SimpleHttpRequest(Method.POST, uriBuilder.build());

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode bodyNode = mapper.createObjectNode();

        bodyNode.put("assistant_id", this.assistantID);
        bodyNode.set("additional_messages", this.convertFewMessagesToJSONArray(guildID, channelID));

        httpRequest.setBody(bodyNode.toString(), ContentType.APPLICATION_JSON);

        return CompletableFuture.supplyAsync(() -> {
            SimpleHttpResponse httpResponse = null;

            try {
                httpResponse = (SimpleHttpResponse) this.get(httpRequest);
            } catch (Exception e) {
                bot.getLogger().log(OpenAIAPIWrapper.class, LogLevel.WARN, "Error getting response from run.");
                throw new RuntimeException(e);
            }

            byte[] bodyBytes = httpResponse.getBodyBytes();

            JsonNode body = null;
            try {
                body = new ObjectMapper().readTree(bodyBytes);
            } catch (IOException e) {
                bot.getLogger().log(OpenAIAPIWrapper.class, LogLevel.WARN, "Error reading response from run.");
                throw new RuntimeException(e);
            }

            String newRunID = body.get("id").asText();
            bot.getLogger().log(OpenAIAPIWrapper.class, LogLevel.DEBUG, "Created Run: " + newRunID);

            return newRunID;
        });

    }

    private CompletableFuture<String> getLatestMessageFromRun(final String threadID, final String runID) throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(String.format("%s/threads/%s/messages", URL, threadID));
        uriBuilder.setParameter("run_id", runID);

        SimpleHttpRequest httpRequest = new SimpleHttpRequest(Method.GET, uriBuilder.build());

        return CompletableFuture.supplyAsync(() -> {
            SimpleHttpResponse httpResponse = null;
            try { httpResponse = (SimpleHttpResponse) this.get(httpRequest); }
            catch (Exception e) { throw new RuntimeException(e); }

            byte[] bodyBytes = httpResponse.getBodyBytes();

            JsonNode body = null;
            try { body = new ObjectMapper().readTree(bodyBytes); }
            catch (IOException e) { throw new RuntimeException(e); }

            String newResponse = body.get("data").get(0).get("content").get(0).get("text").get("value").asText();
            bot.getLogger().log(OpenAIAPIWrapper.class, LogLevel.DEBUG, "Created New Response: " + newResponse);

            return newResponse;
        });
    }

    private CompletableFuture<String> getRunStatus(final String threadID, final String runID) throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(String.format("%s/threads/%s/runs/%s", URL, threadID, runID));

        SimpleHttpRequest httpRequest = new SimpleHttpRequest(Method.GET, uriBuilder.build());

        return CompletableFuture.supplyAsync(() -> {
            SimpleHttpResponse httpResponse = null;
            try { httpResponse = (SimpleHttpResponse) this.get(httpRequest); }
            catch (Exception e) { throw new RuntimeException(e); }

            byte[] bodyBytes = httpResponse.getBodyBytes();

            JsonNode body = null;
            try { body = new ObjectMapper().readTree(bodyBytes); }
            catch (IOException e) { throw new RuntimeException(e); }

            String newRunStatus = body.get("status").asText();
            bot.getLogger().log(OpenAIAPIWrapper.class, LogLevel.DEBUG, "New AI Run Status: " + newRunStatus);

            return newRunStatus;
        });
    }

    private ArrayNode convertFewMessagesToJSONArray(final String guildID, final String channelID) {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode additionalMessagesNode = mapper.createArrayNode();

        Queue<PreviousMessage> messageQueue = previousMessageMap.get(guildID).get(channelID);

        while (!messageQueue.isEmpty()) {
            PreviousMessage message = messageQueue.poll();

            ObjectNode messageNode = mapper.createObjectNode();
            messageNode.put("role", "user");
            messageNode.put("content", String.format("%s: %s", message.getUsername(), message.getContent()));

            additionalMessagesNode.add(messageNode);
        }

        return additionalMessagesNode;
    }

    private HttpResponse get(final SimpleHttpRequest request) throws ExecutionException, InterruptedException, IOException {
        headers.forEach(request::addHeader);

        CloseableHttpAsyncClient client = HttpAsyncClients.custom().build();
        client.start();

        Future<SimpleHttpResponse> future = client.execute(request, null);
        HttpResponse response = future.get();

        client.close();
        return response;
    }

    public CompletableFuture<String> getResponse(final String guildID, final String channelID) throws URISyntaxException {
        // Get active OpenAI Thread
        return this.getThread(guildID)

                // Run assistant on thread
                .thenComposeAsync(
                        (threadID) -> {
                            try {
                                return this.createRun(guildID, channelID, threadID);
                            }
                            catch (URISyntaxException e) {
                                bot.getLogger().log(OpenAIAPIWrapper.class, LogLevel.WARN, "Error running AI on thread.");
                                throw new RuntimeException(e);
                            }
                        }
                )

                // Get assistant's response from thread
                .thenComposeAsync((runID) -> {
                    // Wait until run is complete. Retry 10 times. Once per second.
                    String threadID = aiThreads.get(guildID);

                    String runStatus = "in progress";
                    int count = 0;
                    while (!runStatus.equalsIgnoreCase("completed") && count < 10) {
                        try { runStatus = this.getRunStatus(threadID, runID).get(); count++; Thread.sleep(1000); }
                        catch (Exception e) { throw new RuntimeException(e); }
                    }

                    if (!runStatus.equalsIgnoreCase("completed")) {
                        return CompletableFuture.supplyAsync(() -> "Sorry... I'm really busy with customers right now... I'll talk to you in a bit though!");
                    }

                    try { return this.getLatestMessageFromRun(threadID, runID); }
                    catch (URISyntaxException e) { throw new RuntimeException(e); }
                });
    }

}
