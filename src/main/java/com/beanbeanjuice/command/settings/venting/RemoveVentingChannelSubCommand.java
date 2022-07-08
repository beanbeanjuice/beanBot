package com.beanbeanjuice.command.settings.venting;

import com.beanbeanjuice.utility.command.CommandCategory;
import com.beanbeanjuice.utility.command.ISubCommand;
import com.beanbeanjuice.utility.handler.guild.GuildHandler;
import com.beanbeanjuice.utility.helper.Helper;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * An {@link ISubCommand} used to remove the venting {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}
 * from a specified {@link net.dv8tion.jda.api.entities.Guild Guild}.
 *
 * @author beanbeanjuice
 */
public class RemoveVentingChannelSubCommand implements ISubCommand {

    @Override
    public void handle(@NotNull SlashCommandInteractionEvent event) {
        if (GuildHandler.getCustomGuild(event.getGuild()).setVentingChannelID("0")) {
            event.getHook().sendMessageEmbeds(Helper.successEmbed(
                    "Removed Venting Channel",
                    "Successfully removed the venting channel."
            )).queue();
            return;
        }
        event.getHook().sendMessageEmbeds(Helper.sqlServerError()).queue();
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Remove the venting channel!";
    }

    @NotNull
    @Override
    public String exampleUsage() {
        return "`/venting-channel remove`";
    }

    @NotNull
    @Override
    public CommandCategory getCategoryType() {
        return CommandCategory.SETTINGS;
    }

    @NotNull
    @Override
    public Boolean allowDM() {
        return false;
    }

    @NotNull
    @Override
    public String getName() {
        return "remove";
    }

}
