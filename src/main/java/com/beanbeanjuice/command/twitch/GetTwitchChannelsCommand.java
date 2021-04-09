package com.beanbeanjuice.command.twitch;

import com.beanbeanjuice.main.BeanBot;
import com.beanbeanjuice.utility.command.CommandContext;
import com.beanbeanjuice.utility.command.ICommand;
import com.beanbeanjuice.utility.command.usage.Usage;
import com.beanbeanjuice.utility.command.usage.categories.CategoryType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.ArrayList;

/**
 * A command to get the list of twitch channels.
 *
 * @author beanbeanjuice
 */
public class GetTwitchChannelsCommand implements ICommand {

    @Override
    public void handle(CommandContext ctx, ArrayList<String> args, User user, GuildMessageReceivedEvent event) {

        event.getMessage().delete().queue();

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(BeanBot.getGeneralHelper().getRandomColor());
        embedBuilder.setAuthor("List of Current Twitch Channels");

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("`");

        for (String channel : BeanBot.getGuildHandler().getCustomGuild(event.getGuild().getId()).getTwitch().getTwitchChannelNamesHandler().getTwitchChannelNames()) {
            stringBuilder.append(channel).append("\n");
        }

        if (BeanBot.getGuildHandler().getCustomGuild(event.getGuild().getId()).getTwitch().getTwitchChannelNamesHandler().getTwitchChannelNames().isEmpty()) {
            stringBuilder.append("No channels added.");
        }

        stringBuilder.append("`");
        embedBuilder.setDescription(stringBuilder.toString());
        event.getChannel().sendMessage(embedBuilder.build()).queue();

    }

    @Override
    public String getName() {
        return "get-twitch-channels";
    }

    @Override
    public ArrayList<String> getAliases() {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("gettwitchchannels");
        return arrayList;
    }

    @Override
    public String getDescription() {
        return "Get the list of twitch channels currently in the live channel!";
    }

    @Override
    public Usage getUsage() {
        return new Usage();
    }

    @Override
    public CategoryType getCategoryType() {
        return CategoryType.TWITCH;
    }
}
