package com.beanbeanjuice.command.moderation;

import com.beanbeanjuice.main.BeanBot;
import com.beanbeanjuice.utility.command.CommandContext;
import com.beanbeanjuice.utility.command.ICommand;
import com.beanbeanjuice.utility.command.usage.Usage;
import com.beanbeanjuice.utility.command.usage.categories.CategoryType;
import com.beanbeanjuice.utility.command.usage.types.CommandType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;
import java.util.ArrayList;

/**
 * A command used for muting Discord {@link net.dv8tion.jda.api.entities.Member Members}.
 *
 * @author beanbeanjuice
 */
public class MuteCommand implements ICommand {

    @Override
    public void handle(CommandContext ctx, ArrayList<String> args, User user, GuildMessageReceivedEvent event) {

        event.getMessage().delete().queue();

        if (!BeanBot.getGeneralHelper().checkPermission(event.getMember(), event.getChannel(), Permission.MANAGE_ROLES)) {
            return;
        }

        Role mutedRole = BeanBot.getGuildHandler().getCustomGuild(event.getGuild()).getMutedRole();

        if (mutedRole == null) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setColor(Color.red);
            embedBuilder.setAuthor("Error Muting User");
            embedBuilder.setDescription("A muted role has not been set. Please check the help command.");
            event.getChannel().sendMessage(embedBuilder.build()).queue();
            return;
        }

        Member punishee = event.getGuild().getMember(BeanBot.getGeneralHelper().getUser(args.get(0)));

        event.getGuild().addRoleToMember(punishee, mutedRole).queue();

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setAuthor("Muted User");
        embedBuilder.setColor(BeanBot.getGeneralHelper().getRandomColor());
        embedBuilder.setDescription("Muted " + punishee.getAsMention() + ".");

        event.getChannel().sendMessage(embedBuilder.build()).queue();

    }

    @Override
    public String getName() {
        return "mute";
    }

    @Override
    public ArrayList<String> getAliases() {
        return new ArrayList<>();
    }

    @Override
    public String getDescription() {
        return "Mute a Discord User.";
    }

    @Override
    public Usage getUsage() {
        Usage usage = new Usage();
        usage.addUsage(CommandType.USER, "Discord Mention", true);
        return usage;
    }

    @Override
    public CategoryType getCategoryType() {
        return CategoryType.MODERATION;
    }
}
