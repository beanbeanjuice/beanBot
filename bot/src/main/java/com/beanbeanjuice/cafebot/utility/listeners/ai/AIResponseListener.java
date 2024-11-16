package com.beanbeanjuice.cafebot.utility.listeners.ai;

import com.beanbeanjuice.cafeapi.wrapper.endpoints.guilds.GuildInformationType;
import com.beanbeanjuice.cafebot.CafeBot;
import com.beanbeanjuice.cafebot.utility.api.OpenAIAPIWrapper;
import com.beanbeanjuice.cafebot.utility.helper.Helper;
import com.beanbeanjuice.cafebot.utility.logging.LogLevel;
import com.fasterxml.jackson.databind.JsonNode;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

public class AIResponseListener extends ListenerAdapter {

    private final CafeBot cafeBot;
    private final HashMap<List<String>, List<String>> messageMap;
    private final OpenAIAPIWrapper openAI;

    // Map of guild IDs, containing a map of channel IDs with a list of messages
    private final Map<String, Map<String, Queue<PreviousMessage>>> previousMessageMap;

    public AIResponseListener(final CafeBot cafeBot, final String openAIAPIKey, final String openAIAssistantID) {
        this.cafeBot = cafeBot;
        this.messageMap = new HashMap<>();

        previousMessageMap = new HashMap<>();
        this.openAI = new OpenAIAPIWrapper(cafeBot, openAIAPIKey, openAIAssistantID, previousMessageMap);
        this.openAI.setHeaders();
        refreshMaps();
    }

    public void refreshMaps() {
        this.messageMap.clear();

        try {
            createMaps();
        } catch (IOException e) {
            cafeBot.getLogger().log(AIResponseListener.class, LogLevel.ERROR, "Unable to refresh AI maps...", true, true, e);
        }
    }

    private void createMaps() throws IOException {
        for (JsonNode type : Helper.parseJson("ai.json")) {
            List<String> triggers = new ArrayList<>();
            List<String> responses = new ArrayList<>();

            for (JsonNode trigger : type.get("triggers")) triggers.add(trigger.asText());
            for (JsonNode response : type.get("responses")) responses.add(response.asText());

            messageMap.put(triggers, responses);
        }
    }

    private void addMessageToGuild(final MessageReceivedEvent event) {
        previousMessageMap.putIfAbsent(event.getGuild().getId(), new HashMap<>());

        Map<String, Queue<PreviousMessage>> previousMessageMapForGuild = previousMessageMap.get(event.getGuild().getId());
        previousMessageMapForGuild.putIfAbsent(event.getChannel().getId(), new LinkedList<>());

        String message = event.getMessage().getContentDisplay();
        if (message.length() >= 500) {
            message = message.substring(0, 500);
            message = message + "... (more was shown but just try to ignore the rest)";
        }

        previousMessageMapForGuild.get(event.getChannel().getId()).offer(new PreviousMessage(message, event.getAuthor().getName()));

        if (previousMessageMapForGuild.get(event.getChannel().getId()).size() > 10)
            previousMessageMapForGuild.get(event.getChannel().getId()).poll();
    }

    private void handleOpenAIResponse(final MessageReceivedEvent event) {
        try {
            event.getChannel().sendTyping().queue();

            cafeBot.getLogger().log(AIResponseListener.class, LogLevel.INFO, String.format("Running AI For User (%s) on Guild (%s): %s", event.getAuthor().getId(), event.getGuild().getId(), event.getMessage().getContentRaw()), true, false);
            openAI.getResponse(event.getGuild().getId(), event.getChannel().getId())
                    .thenAcceptAsync((response) -> event.getMessage().reply(response).queue());
        } catch (URISyntaxException e) {
            event.getMessage().reply(e.getMessage()).queue();
        }
    }

    @Override
    public void onMessageReceived(final MessageReceivedEvent event) {
        if (!event.isFromGuild()) return;
        if (event.getAuthor().isBot()) return;

        String guildID = event.getGuild().getId();

        cafeBot.getCafeAPI().getGuildsEndpoint().getGuildInformation(guildID).thenAcceptAsync((information) -> {
            boolean useAI = Boolean.parseBoolean(information.getSetting(GuildInformationType.AI_RESPONSE));
            if (!useAI) return;
            this.addMessageToGuild(event);

//            if (event.getMessage().getReferencedMessage().getAuthor().equals(this.cafeBot.getSelfUser())) {
//                // TODO: Handle reply response.
//                return;
//            }

            if (event.getMessage().getMentions().isMentioned(this.cafeBot.getSelfUser())) {
                this.handleOpenAIResponse(event);
                return;
            }

//            String message = event.getMessage().getContentRaw().replaceAll("[^\\sa-zA-Z0-9]", "").toLowerCase();
//
//            messageMap.forEach((commandTerms, commandResponses) -> {
//                if (commandTerms.stream().noneMatch(message::contains)) return;
//
//                // TODO: Handle trigger word response.
//                event.getMessage().reply(parseMessage(
//                        commandResponses.get(Helper.getRandomInteger(0, commandResponses.size())),
//                        event.getAuthor()
//                )).queue();
//                cafeBot.increaseCommandsRun();
//            });
        });
    }

    private String parseMessage(final String message, final User user) {
        return message.replace("{user}", user.getAsMention());
    }

}
