package com.beanbeanjuice.command.generic;

import com.beanbeanjuice.main.BeanBot;
import com.beanbeanjuice.utility.command.CommandContext;
import com.beanbeanjuice.utility.command.ICommand;
import com.beanbeanjuice.utility.command.usage.Usage;
import com.beanbeanjuice.utility.command.usage.categories.CategoryType;
import com.beanbeanjuice.utility.command.usage.types.CommandType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * A general ping command to show bot information.
 *
 * @author beanbeanjuice
 */
public class PingCommand implements ICommand {

    @Override
    public void handle(CommandContext ctx, ArrayList<String> args, User user, GuildMessageReceivedEvent event) {
        BeanBot.getJDA().getRestPing().queue(
                (ping) -> event.getChannel()
                        .sendMessage(messageEmbed(ping, BeanBot.getJDA().getGatewayPing())).queue()
        );
    }

    @NotNull
    private MessageEmbed messageEmbed(@NotNull Long botPing, @NotNull Long gatewayPing) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setAuthor(getName() + "!", "https://www.beanbeanjuice.com/beanBot.html");
        embedBuilder.addField("Rest Ping", botPing.toString(), true);
        embedBuilder.addField("Gateway Ping", gatewayPing.toString(), true);
        embedBuilder.addField("Current Version", BeanBot.getBotVersion(), true);
        embedBuilder.setFooter("Author: beanbeanjuice - " + "https://github.com/beanbeanjuice/beanBot/issues");
        embedBuilder.setThumbnail(BeanBot.getJDA().getSelfUser().getAvatarUrl());
        embedBuilder.setColor(BeanBot.getGeneralHelper().getRandomColor());
        embedBuilder.setDescription("Hello there! How are you!~ Want some coffee?");
        return embedBuilder.build();
    }

    @Override
    public String getName() {
        return "ping";
    }

    @Override
    public ArrayList<String> getAliases() {
        return new ArrayList<>();
    }

    @Override
    public String getDescription() {
        return "Ping the bot!";
    }

    @Override
    public Usage getUsage() {
        Usage usage = new Usage();
        usage.addUsage(CommandType.TEXT, "Any Text", false);
        usage.addUsage(CommandType.LINK, "Any Link", false);
        return usage;
    }

    @Override
    public CategoryType getCategoryType() {
        return CategoryType.GENERIC;
    }
}