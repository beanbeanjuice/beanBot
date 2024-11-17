package com.beanbeanjuice.cafebot.utility.listeners;

import com.beanbeanjuice.cafeapi.wrapper.endpoints.mutualguilds.MutualGuildsEndpoint;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class MutualGuildsListener extends ListenerAdapter {

    private final MutualGuildsEndpoint mutualGuildsEndpoint;
    private final HashMap<String, ArrayList<String>> mutualGuildsCache;

    public MutualGuildsListener(MutualGuildsEndpoint mutualGuildsEndpoint) {
        this.mutualGuildsEndpoint = mutualGuildsEndpoint;
        this.mutualGuildsCache = new HashMap<>();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.isFromGuild()) return;
        if (event.getAuthor().isBot()) return;

        String guildID = event.getGuild().getId();
        String userID = event.getAuthor().getId();

        this.addMutualGuild(userID, guildID);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.isFromGuild()) return;
        if (event.getUser().isBot()) return;

        String guildID = event.getGuild().getId();
        String userID = event.getUser().getId();

        this.addMutualGuild(userID, guildID);
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (!event.isFromGuild()) return;
        if (event.getUser().isBot()) return;

        String guildID = event.getUser().getId();
        String userID = event.getUser().getId();

        this.addMutualGuild(userID, guildID);
    }

    @Override
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
        if (!event.isFromGuild()) return;
        if (event.getUser().isBot()) return;

        String guildID = event.getUser().getId();
        String userID = event.getUser().getId();

        this.addMutualGuild(userID, guildID);
    }

    private void addMutualGuild(String userID, String guildID) {
        this.mutualGuildsEndpoint.addMutualGuild(userID, guildID);

        this.mutualGuildsCache.putIfAbsent(guildID, new ArrayList<>());
        this.mutualGuildsCache.get(userID).add(guildID);
    }

}
