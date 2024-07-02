package com.beanbeanjuice.cafebot.commands.interaction;

import com.beanbeanjuice.cafeapi.wrapper.endpoints.interactions.InteractionType;
import com.beanbeanjuice.cafebot.CafeBot;
import com.beanbeanjuice.cafebot.utility.commands.Command;
import com.beanbeanjuice.cafebot.utility.commands.ICommand;
import com.beanbeanjuice.cafebot.utility.sections.interactions.ICommandInteraction;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class WinkCommand extends Command implements ICommand, ICommandInteraction {

    public WinkCommand(final CafeBot cafeBot) {
        super(cafeBot);
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        this.handleInteraction(InteractionType.WINK, event, cafeBot);
    }

    @Override
    public String getName() {
        return "wink";
    }

    @Override
    public String getDescription() {
        return "Wink at someone! ;)";
    }

    @Override
    public OptionData[] getOptions() {
        return new OptionData[] {
                new OptionData(OptionType.USER, "user", "The user you want to wink at."),
                new OptionData(OptionType.STRING, "message", "An optional message you can send.")
        };
    }

    @Override
    public Permission[] getPermissions() {
        return new Permission[0];
    }

    @Override
    public boolean isEphemeral() {
        return false;
    }

    @Override
    public boolean isNSFW() {
        return false;
    }

    @Override
    public boolean allowDM() {
        return true;
    }

    @Override
    public String getSelfString() {
        return "%s **winked** at themself... <:disgusted:1257142116539301909>";
    }

    @Override
    public String getOtherString() {
        return "%s **winked** at %s! <:pleading_blush:1257143682776432731>";
    }

    @Override
    public String getBotString() {
        return "Gross. <:disgusted:1257142116539301909>";
    }

    @Override
    public String getFooterString() {
        return "%s winked at %d people. %s was winked at %d times.";
    }

}
