package com.beanbeanjuice.cafebot.command.interaction;

import com.beanbeanjuice.cafebot.utility.command.CommandCategory;
import com.beanbeanjuice.cafebot.utility.command.ICommand;
import com.beanbeanjuice.cafebot.utility.section.interaction.Interaction;
import com.beanbeanjuice.cafeapi.wrapper.endpoints.interactions.InteractionType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * An {@link ICommand} used to rage at people.
 *
 * @author beanbeanjuice
 */
public class RageCommand implements ICommand {

    @Override
    public void handle(@NotNull SlashCommandInteractionEvent event) {
        new Interaction(InteractionType.RAGE,
                "**{sender}** *is raging*! You- you should calm down... <:cafeBot_angry:1171726164092518441>",
                "**{sender}** *is raging* at **{receiver}**!",
                "{sender} raged at others {amount_sent} times. {receiver} was raged at {amount_received} times.",
                "DON'T RAGE AT ME <a:man_scream:841921434732724224>",
                event);
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Rage at someone.";
    }

    @NotNull
    @Override
    public String exampleUsage() {
        return "`/rage` or `/rage @beanbeanjuice`";
    }

    @NotNull
    @Override
    public ArrayList<OptionData> getOptions() {
        ArrayList<OptionData> options = new ArrayList<>();
        options.add(new OptionData(OptionType.USER, "receiver", "The person to rage at.", false, false));
        options.add(new OptionData(OptionType.STRING, "message", "An optional message to add.", false, false));
        return options;
    }

    @NotNull
    @Override
    public CommandCategory getCategoryType() {
        return CommandCategory.INTERACTION;
    }

    @NotNull
    @Override
    public Boolean allowDM() {
        return true;
    }

}
