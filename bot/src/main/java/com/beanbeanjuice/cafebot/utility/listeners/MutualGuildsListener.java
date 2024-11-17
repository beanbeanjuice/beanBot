package com.beanbeanjuice.cafebot.utility.listeners;

import com.beanbeanjuice.cafeapi.wrapper.endpoints.mutualguilds.MutualGuildsEndpoint;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@RequiredArgsConstructor
public class MutualGuildsListener extends ListenerAdapter {

    private final MutualGuildsEndpoint mutualGuildsEndpoint;

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.isFromGuild()) return;
        if (event.getAuthor().isBot()) return;

        String guildID = event.getGuild().getId();
        String userID = event.getAuthor().getId();

        this.mutualGuildsEndpoint.addMutualGuild(userID, guildID);
    }

}
