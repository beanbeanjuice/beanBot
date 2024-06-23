package com.beanbeanjuice.cafebot.utility.section.twitch;

import com.beanbeanjuice.cafebot.Bot;
import com.beanbeanjuice.cafebot.utility.handler.guild.CustomGuild;
import com.beanbeanjuice.cafebot.utility.handler.guild.GuildHandler;
import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.philippheuer.events4j.simple.domain.EventSubscriber;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.github.twitch4j.helix.domain.UserList;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A class for handling twitch message events.
 *
 * @author beanbeanjuice
 */
public class TwitchMessageEventHandler extends SimpleEventHandler {

    /**
     * @param event The {@link ChannelGoLiveEvent}.
     */
    @EventSubscriber
    public void onChannelGoLive(final ChannelGoLiveEvent event) {
        String twitchName = event.getChannel().getName().toLowerCase();
        ArrayList<String> guilds = TwitchHandler.getGuildsForChannel(twitchName);

        // If there are no guilds/sql error, do nothing.
        if (guilds.isEmpty()) return;

        // Go through each guild.
        guilds.forEach((guildID) -> {
            CustomGuild customGuild = GuildHandler.getCustomGuild(guildID);

            if (!customGuild.getTwitchChannels().contains(twitchName)) return;

            String liveChannelID = GuildHandler.getCustomGuild(guildID).getLiveChannelID();
            TextChannel liveChannel = GuildHandler.getGuild(guildID).getTextChannelById(liveChannelID);
            StringBuilder message = new StringBuilder();

            GuildHandler.getCustomGuild(guildID).getLiveNotificationsRole()
                    .ifPresent((liveNotificationsRole) -> message.append(liveNotificationsRole).append(", "));

            message.append(event.getChannel().getName())
                    .append(", is now live on ")
                    .append("https://www.twitch.tv/")
                    .append(event.getChannel().getName());

            try {
                liveChannel.sendMessage(message.toString()).setEmbeds(liveEmbed(event)).queue();
                Bot.commandsRun++;
            } catch (NullPointerException ignored) { } // If the live channel no longer exists, then just don't print the message.
        });
    }

    /**
     * Creates the {@link MessageEmbed} to be sent in the LiveChannel.
     * @param event The {@link ChannelGoLiveEvent}.
     * @return The {@link MessageEmbed} to be sent.
     */
    public MessageEmbed liveEmbed(final ChannelGoLiveEvent event) {
        String channelName = event.getChannel().getName();
        String userProfileImage = getUserProfileImage(channelName);
        return new EmbedBuilder()
                .setColor(Color.pink)
                .setAuthor(event.getChannel().getName(), null, userProfileImage)
                .setTitle(event.getStream().getTitle(), "https://www.twitch.tv/" + channelName)
                .setImage(event.getStream().getThumbnailUrl(320, 180))
                .setThumbnail(userProfileImage)
                .addField("Game", event.getStream().getGameName(), true)
                .addField("Viewers", String.valueOf(event.getStream().getViewerCount()), true)
                .setFooter("Live information brought to you by cafeBot!")
                .build();
    }

    private String getUserProfileImage(final String user) {
        UserList userList = TwitchHandler.getTwitchListener().getTwitchClient().getHelix()
                .getUsers(null, null, List.of(user)).execute();

        return userList.getUsers().getFirst().getProfileImageUrl();
    }

}
