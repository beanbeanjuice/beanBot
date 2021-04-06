package com.beanbeanjuice.command.moderation;

import com.beanbeanjuice.main.BeanBot;
import com.beanbeanjuice.utility.command.CommandContext;
import com.beanbeanjuice.utility.command.ICommand;
import com.beanbeanjuice.utility.command.usage.Usage;
import com.beanbeanjuice.utility.command.usage.categories.CategoryType;
import com.beanbeanjuice.utility.command.usage.types.CommandType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;
import java.util.ArrayList;

/**
 * A command to set the moderator role.
 *
 * @author beanbeanjuice
 */
public class SetModeratorRoleCommand implements ICommand {

    @Override
    public void handle(CommandContext ctx, ArrayList<String> args, User user, GuildMessageReceivedEvent event) {

        if (!BeanBot.getGeneralHelper().isAdministrator(event.getMember(), event)) {
            return;
        }

        event.getMessage().delete().queue();

        String argument = args.get(0).replace("<@&", "").replace(">", "");

        Role role = event.getGuild().getRoleById(argument);

        if (role == null) {

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setColor(Color.red);
            embedBuilder.setAuthor("Unknown Role");
            embedBuilder.setDescription("`" + argument + "` is not a role.");

            event.getChannel().sendMessage(embedBuilder.build()).queue();
            return;

        }

        if (!BeanBot.getGuildHandler().updateGuildModeratorRole(event.getGuild(), role)) {
            event.getChannel().sendMessage(BeanBot.getGeneralHelper().sqlServerError());
            return;
        }

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.green);
        embedBuilder.setAuthor("Successfully changed the Moderator Role");
        embedBuilder.setDescription("Successfully changed the moderator role to " + role.getAsMention());
        event.getChannel().sendMessage(embedBuilder.build()).queue();

    }

    @Override
    public String getName() {
        return "set-moderator-role";
    }

    @Override
    public ArrayList<String> getAliases() {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("setmoderatorrole");
        arrayList.add("setmodrole");
        arrayList.add("set-mod-role");
        return arrayList;
    }

    @Override
    public String getDescription() {
        return "Sets the moderator role for the server.";
    }

    @Override
    public Usage getUsage() {
        Usage usage = new Usage();
        usage.addUsage(CommandType.TEXT, "Role ID or Role Mention", true);
        return usage;
    }

    @Override
    public CategoryType getCategoryType() {
        return CategoryType.MODERATION;
    }
}
