package com.beanbeanjuice.cafebot.utility.sections.fun.birthday;

import com.beanbeanjuice.cafeapi.wrapper.endpoints.birthdays.Birthday;
import com.beanbeanjuice.cafeapi.wrapper.endpoints.guilds.GuildInformationType;
import com.beanbeanjuice.cafebot.CafeBot;
import com.beanbeanjuice.cafebot.utility.helper.Helper;
import com.beanbeanjuice.cafebot.utility.logging.LogLevel;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class BirthdayHelper {

    private final CafeBot cafeBot;
    private final Map<String, Long> lastChangedTimes;
    @Getter private final long waitTime = TimeUnit.DAYS.toMillis(7);

    public BirthdayHelper(final CafeBot cafeBot) {
        this.cafeBot = cafeBot;
        this.lastChangedTimes = new HashMap<>();
    }

    public void startBirthdayChecker() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                cafeBot.getLogger().log(BirthdayHelper.class, LogLevel.INFO, "Checking birthdays...", true, false);
                checkBirthdays();
            }
        };

        // Start on the next hour.
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(task, calendar.getTime(), TimeUnit.HOURS.toMillis(1));
    }

    private void checkBirthdays() {
        this.cafeBot.getCafeAPI().getBirthdaysEndpoint().getAllBirthdays().thenAcceptAsync((birthdayMap) -> {
            birthdayMap.entrySet().stream().filter((entry) -> entry.getValue().isBirthday()).forEach((entry) -> {
                this.cafeBot.getLogger().log(BirthdayHelper.class, LogLevel.INFO, String.format("Checking Birthday: %s", entry.getKey()), true, false);
                sendBirthdayWish(entry.getKey(), entry.getValue());
            });
        });
    }

    private void sendBirthdayWish(final String userID, final Birthday birthday) {
        cafeBot.getShardManager().retrieveUserById(userID).queue((user) -> {
            this.cafeBot.getLogger().log(BirthdayHelper.class, LogLevel.INFO, String.format("Sending Birthday Messages: %s", userID), true, false);

            user.openPrivateChannel().queue((privateChannel) -> {
                privateChannel.sendMessage(
                        """
                        Psst... It's busy here at the cafe right now but I just
                        wanted to wish you a happy birthday! I saved some cake for you~ 🍰
                        I hope you have an amazing day with lots of presents! <a:cafeBot:1119635469727191190>
                        """
                ).queue();
            });

            this.cafeBot.getCafeAPI()
                    .getMutualGuildsEndpoint()
                    .getMutualGuilds(user.getId())
                    .thenAccept((mutualGuildIDs) -> sendBirthdayMessageToEveryServer(mutualGuildIDs, user, birthday));
        });
    }

    private void sendBirthdayMessageToEveryServer(final List<String> guildIDs, final User user, final Birthday birthday) {
        guildIDs.forEach((guildID) -> {
            Optional<Guild> guildOptional = Optional.ofNullable(cafeBot.getShardManager().getGuildById(guildID));
            if (guildOptional.isEmpty()) return;

            Guild guild = guildOptional.get();

            this.cafeBot.getLogger().log(BirthdayHelper.class, LogLevel.INFO, String.format("Sending Birthday To Guild: %s", guild.getId()), true, false);
            this.cafeBot.getCafeAPI()
                    .getGuildsEndpoint()
                    .getGuildInformation(guild.getId())
                    .thenAcceptAsync((guildInformation) -> handleGuildBirthday(guild, guildInformation.getSetting(GuildInformationType.BIRTHDAY_CHANNEL_ID), user, birthday));
        });
    }

    private void handleGuildBirthday(final Guild guild, final String birthdayChannelID, final User user, final Birthday birthday) {
        TextChannel birthdayChannel = guild.getTextChannelById(birthdayChannelID);
        if (birthdayChannel == null) return;

        birthdayChannel.sendMessage("It's someone's birthday! 🥳").addEmbeds(birthdayEmbed(user, birthday)).queue();
    }

    private MessageEmbed birthdayEmbed(final User user, final Birthday birthday) {
        return new EmbedBuilder()
                .setAuthor(user.getName(), null, user.getAvatarUrl())
                .setDescription(String.format("%s was born on %s %d! Wish them a happy birthday! 🎂", user.getAsMention(), birthday.getMonth().toString(), birthday.getDay()))
                .setColor(Helper.getRandomColor())
                .build();
    }

    public boolean canChangeBirthday(final String userID) {
        return this.getElapsedTime(userID).map(elapsedTime -> elapsedTime > waitTime).orElse(true);
    }

    public Optional<Long> getElapsedTime(final String userID) {
        if (!lastChangedTimes.containsKey(userID)) return Optional.empty();
        long lastChangedTime = lastChangedTimes.get(userID);
        long currentTime = System.currentTimeMillis();
        return Optional.of(currentTime - lastChangedTime);
    }

    public void addUserCooldown(final String userID) {
        lastChangedTimes.put(userID, System.currentTimeMillis());
    }

}
