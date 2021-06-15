package com.beanbeanjuice.command.moderation;

import com.beanbeanjuice.CafeBot;
import com.beanbeanjuice.utility.command.CommandContext;
import com.beanbeanjuice.utility.command.ICommand;
import com.beanbeanjuice.utility.command.usage.Usage;
import com.beanbeanjuice.utility.command.usage.categories.CategoryType;
import com.beanbeanjuice.utility.command.usage.types.CommandType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

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
        if (!CafeBot.getGeneralHelper().isAdministrator(event.getMember(), event)) {
            return;
        }

        String argument = args.get(0).replace("<@&", "").replace(">", "");

        Role role = event.getGuild().getRoleById(argument);

        if (args.get(0).equals("0")) {
            if (CafeBot.getGuildHandler().getCustomGuild(event.getGuild()).setModeratorRoleID("0")) {
                event.getChannel().sendMessage(CafeBot.getGeneralHelper().successEmbed(
                        "Removed Moderator Role",
                        "Successfully removed the moderator role."
                )).queue();
                return;
            }
            event.getChannel().sendMessage(CafeBot.getGeneralHelper().sqlServerError()).queue();
            return;
        }

        if (role == null) {
            event.getChannel().sendMessage(unknownRoleEmbed(argument)).queue();
            return;
        }

        if (!CafeBot.getGuildHandler().getCustomGuild(event.getGuild()).setModeratorRoleID(role.getId())) {
            event.getChannel().sendMessage(CafeBot.getGeneralHelper().sqlServerError()).queue();
            return;
        }

        event.getChannel().sendMessage(successfulRoleChangeEmbed(role)).queue();
    }

    @NotNull
    private MessageEmbed successfulRoleChangeEmbed(@NotNull Role role) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(CafeBot.getGeneralHelper().getRandomColor());
        embedBuilder.setTitle("Successfully changed the Moderator Role");
        embedBuilder.setDescription("Successfully changed the moderator role to " + role.getAsMention());
        return embedBuilder.build();
    }

    @NotNull
    private MessageEmbed unknownRoleEmbed(@NotNull String roleName) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.red);
        embedBuilder.setTitle("Unknown Role");
        embedBuilder.setDescription("`" + roleName + "` is not a role.");
        return embedBuilder.build();
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
    public String exampleUsage(String prefix) {
        return "`" + prefix + "setmoderatorrole 0` or `" + prefix + "setmoderatorrole @ModRole`";
    }

    @Override
    public Usage getUsage() {
        Usage usage = new Usage();
        usage.addUsage(CommandType.TEXT, "Role Mention/ID", true);
        return usage;
    }

    @Override
    public CategoryType getCategoryType() {
        return CategoryType.MODERATION;
    }
}
