package com.beanbeanjuice.cafebot.commands.fun.birthday.self;

import com.beanbeanjuice.cafeapi.wrapper.endpoints.birthdays.Birthday;
import com.beanbeanjuice.cafeapi.wrapper.endpoints.birthdays.BirthdayMonth;
import com.beanbeanjuice.cafeapi.wrapper.endpoints.birthdays.BirthdaysEndpoint;
import com.beanbeanjuice.cafebot.CafeBot;
import com.beanbeanjuice.cafebot.utility.commands.Command;
import com.beanbeanjuice.cafebot.utility.commands.ISubCommand;
import com.beanbeanjuice.cafebot.utility.helper.Helper;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class BirthdaySetSubCommand extends Command implements ISubCommand {

    public BirthdaySetSubCommand(final CafeBot cafeBot) {
        super(cafeBot);
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        BirthdayMonth month = BirthdayMonth.valueOf(event.getOption("month").getAsString());
        int day = event.getOption("day").getAsInt();
        TimeZone timeZone = TimeZone.getTimeZone(event.getOption("timezone").getAsString());

        if (month.getDaysInMonth() < day) {
            invalidMonthAndDayCombination(month, day, event);
            return;
        }

        User user = event.getUser();

        if (!cafeBot.getBirthdayHelper().canChangeBirthday(user.getId())) {
            long waitTime = this.cafeBot.getBirthdayHelper().getWaitTime();

            event.getHook().sendMessageEmbeds(Helper.errorEmbed(
                    "Error Setting Birthday",
                    String.format
                            ("You can only change your birthday once a week. Remaining Time: **%s** hours.",
                                    TimeUnit.MILLISECONDS.toHours(waitTime - this.cafeBot.getBirthdayHelper().getElapsedTime(user.getId()).orElse(waitTime))
                            )
            )).queue();
            return;
        }

        updateUserBirthday(month, day, timeZone, event);
    }



    private void updateUserBirthday(final BirthdayMonth month, final int day, final TimeZone timeZone, final SlashCommandInteractionEvent event) {
        User user = event.getUser();

        Birthday birthday = new Birthday(month, day, timeZone.getID());
        BirthdaysEndpoint birthdaysEndpoint = cafeBot.getCafeAPI().getBirthdaysEndpoint();

        birthdaysEndpoint.createUserBirthday(user.getId(), birthday)
                .exceptionallyComposeAsync((e) -> birthdaysEndpoint.updateUserBirthday(user.getId(), birthday))
                .thenAcceptAsync((ignored) -> {
                    this.cafeBot.getBirthdayHelper().addUserCooldown(user.getId());

                    event.getHook().sendMessageEmbeds(Helper.successEmbed(
                            "Birthday Set",
                            String.format("You have successfully set your birthday to **%s %d** on *%s*.", month, day, timeZone.getDisplayName())
                    )).queue();
                })
                .exceptionallyAsync((e) -> {
                    event.getHook().sendMessageEmbeds(Helper.errorEmbed(
                            "Error Setting Birthday",
                            String.format("There was an error setting your birthday: %s", e.getMessage())
                    )).queue();
                    return null;
                });
    }

    private void invalidMonthAndDayCombination(final BirthdayMonth month, final int day, final SlashCommandInteractionEvent event) {
        event.getHook().sendMessageEmbeds(Helper.errorEmbed(
                "Invalid Month and Day Combo",
                String.format("There are not **%d** days in **%s**.", day, month.toString())
        )).queue();
    }

    @Override
    public String getName() {
        return "set";
    }

    @Override
    public String getDescription() {
        return "Edit your birthday!";
    }

    @Override
    public OptionData[] getOptions() {
        return new OptionData[]{
                new OptionData(OptionType.STRING, "month", "The month you were born!", true)
                        .addChoice("1 - January", "JANUARY")
                        .addChoice("2 - February", "FEBRUARY")
                        .addChoice("3 - March", "MARCH")
                        .addChoice("4 - April", "APRIL")
                        .addChoice("5 - May", "MAY")
                        .addChoice("6 - June", "JUNE")
                        .addChoice("7 - July", "JULY")
                        .addChoice("8 - August", "AUGUST")
                        .addChoice("9 - September", "SEPTEMBER")
                        .addChoice("10 - October", "OCTOBER")
                        .addChoice("11 - November", "NOVEMBER")
                        .addChoice("12 - December", "DECEMBER"),

                new OptionData(OptionType.INTEGER, "day", "The day you were born in the specified month!", true)
                        .setRequiredRange(1, 31),

                new OptionData(OptionType.STRING, "timezone", "Start typing to see available options!", true, true)
        };
    }

    @Override
    public HashMap<String, ArrayList<String>> getAutoComplete() {
        HashMap<String, ArrayList<String>> autoCompleteMap = new HashMap<>();
        autoCompleteMap.put("timezone", new ArrayList<>());

        for (String timezone : TimeZone.getAvailableIDs())
            autoCompleteMap.get("timezone").add(timezone);

        return autoCompleteMap;
    }

}
