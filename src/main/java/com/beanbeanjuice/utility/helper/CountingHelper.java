package com.beanbeanjuice.utility.helper;

import com.beanbeanjuice.CafeBot;
import com.beanbeanjuice.utility.logger.LogLevel;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.sql.*;

/**
 * A class used for helping with counting.
 *
 * @author beanbeanjuice
 */
public class CountingHelper {

    /**
     * Checks the current number for the {@link Guild}.
     * @param event The {@link GuildMessageReceivedEvent} for the {@link Guild}.
     * @param currentNumber The current number for the {@link Guild}.
     */
    public void checkNumber(@NotNull GuildMessageReceivedEvent event, @NotNull Integer currentNumber) {
        Guild guild = event.getGuild();

        Integer lastNumber = getLastNumber(guild);
        Integer highestNumber = getHighestNumber(guild);
        String lastUserID = getLastUserID(guild);

        if (lastNumber == null || highestNumber == null || lastUserID == null) {
            event.getChannel().sendMessageEmbeds(CafeBot.getGeneralHelper().sqlServerError()).queue();
            return;
        }

        if (currentNumber == (lastNumber+1) && !lastUserID.equals(event.getAuthor().getId())) {

            // Set the last number to the current number
            // If it fails, say so and return.
            if (!setLastNumber(guild, currentNumber)) {
                event.getChannel().sendMessageEmbeds(CafeBot.getGeneralHelper().sqlServerError()).queue();
                return;
            }

            // Checking if the current number is now the highest number.
            if (currentNumber > highestNumber) {
                // Set the highest number to the current number.
                // If it fails, say so and return.
                if (!setHighestNumber(guild, currentNumber)) {
                    event.getChannel().sendMessageEmbeds(CafeBot.getGeneralHelper().sqlServerError()).queue();
                    return;
                }
                event.getMessage().addReaction("☑").queue(); // Blue Checkmark Reaction
            } else {
                event.getMessage().addReaction("U+2705").queue(); // Green Checkmark Reaction
            }

            // Checks if able to set the last user ID.
            if (!setLastUserID(guild, event.getAuthor().getId())) {
                event.getChannel().sendMessageEmbeds(CafeBot.getGeneralHelper().sqlServerError()).queue();
                return;
            }

            // Checks if the current number is divisible by 100.
            if (currentNumber % 100 == 0) {
                event.getMessage().addReaction("U+1F31F").queue(); // Star Reaction for if they get to a number that is divisible by 100.
            }

        } else {

            // Checks if able to set the last number back to 0.
            if (!setLastNumber(guild, 0)) {
                event.getChannel().sendMessageEmbeds(CafeBot.getGeneralHelper().sqlServerError()).queue();
                return;
            }

            // Checks if able to set the last user ID to 0.
            if (!setLastUserID(guild, "0")) {
                event.getChannel().sendMessageEmbeds(CafeBot.getGeneralHelper().sqlServerError()).queue();
                return;
            }

            event.getMessage().addReaction("U+274C").queue();
            event.getChannel().sendMessageEmbeds(failedEmbed(event.getMember(), lastNumber, highestNumber)).queue();
        }
    }

    /**
     * Get the leaderboard place for a specified {@link Integer} limit.
     * @param limit The limit specified.
     * @return The current place of that {@link Integer}.
     */
    @Nullable
    public Integer getCountingLeaderboardPlace(@NotNull Integer limit) {
        Connection connection = CafeBot.getSQLServer().getConnection();
        String arguments = "SELECT * FROM cafeBot.counting_information WHERE counting_information.highest_number>=(?) ORDER BY counting_information.highest_number DESC;";

        try {
            PreparedStatement statement = connection.prepareStatement(arguments);
            statement.setInt(1, limit);
            ResultSet resultSet = statement.executeQuery();
            int count = 0;
            while (resultSet.next()) {
                count++;
            }
            return count;
        } catch (SQLException e) {
            CafeBot.getLogManager().log(CountingHelper.class, LogLevel.WARN, "Error Getting Leaderboard: " + e.getMessage());
            return null;
        }
    }

    /**
     * Sets the last user ID for the counting {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.
     * @param guild The {@link Guild} specified.
     * @param lastUserID The ID of the last user who sent the message.
     * @return Whether or not setting the last user ID was successful.
     */
    @NotNull
    private Boolean setLastUserID(@NotNull Guild guild, @NotNull String lastUserID) {
        Connection connection = CafeBot.getSQLServer().getConnection();
        String arguments = "UPDATE cafeBot.counting_information SET last_user_id = (?) WHERE guild_id = (?);";

        try {
            PreparedStatement statement = connection.prepareStatement(arguments);
            statement.setLong(1, Long.parseLong(lastUserID));
            statement.setLong(2, Long.parseLong(guild.getId()));
            statement.execute();
            return true;
        } catch (SQLException e) {
            CafeBot.getLogManager().log(this.getClass(), LogLevel.WARN, "Error Setting Last User ID: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * @param guild The {@link Guild} specified.
     * @return The ID of the last user who sent the counting try.
     */
    @Nullable
    private String getLastUserID(@NotNull Guild guild) {
        Connection connection = CafeBot.getSQLServer().getConnection();
        String arguments = "SELECT * FROM cafeBot.counting_information WHERE guild_id = (?);";

        try {
            PreparedStatement statement = connection.prepareStatement(arguments);
            statement.setLong(1, Long.parseLong(guild.getId()));
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet.getString(4);
        } catch (SQLException e) {
            CafeBot.getLogManager().log(this.getClass(), LogLevel.WARN, "Error Getting Last User ID: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * A failed {@link MessageEmbed} to send if they fail the counting.
     * @param member The {@link Member} who failed.
     * @param lastNumber The last number entered.
     * @param highestNumber The current highest number.
     * @return The {@link MessageEmbed} to send.
     */
    @NotNull
    private MessageEmbed failedEmbed(@NotNull Member member, @NotNull Integer lastNumber, @NotNull Integer highestNumber) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Counting Failed");
        embedBuilder.setDescription("Counting failed due to " + member.getAsMention() + " at `" + lastNumber + "`. " +
                "The highest number received on this server was `" + highestNumber + "`. Counting has been reset to `0`. " +
                "Remember, the same user can't count twice in a row and the numbers must increment by 1!");
        embedBuilder.setColor(Color.red);
        return embedBuilder.build();
    }

    /**
     * Sets the highest number for the {@link Guild}.
     * @param guild The {@link Guild} specified.
     * @param currentNumber The current number for the {@link Guild}.
     * @return Whether or not setting the highest number was successful.
     */
    @NotNull
    private Boolean setHighestNumber(@NotNull Guild guild, @NotNull Integer currentNumber) {
        Connection connection = CafeBot.getSQLServer().getConnection();
        String arguments = "UPDATE cafeBot.counting_information SET highest_number = (?) WHERE guild_id = (?);";

        try {
            PreparedStatement statement = connection.prepareStatement(arguments);
            statement.setInt(1, currentNumber);
            statement.setLong(2, Long.parseLong(guild.getId()));
            statement.execute();
            return true;
        } catch (SQLException e) {
            CafeBot.getLogManager().log(this.getClass(), LogLevel.WARN, "Error Setting Highest Number: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Sets the last number for the {@link Guild}.
     * @param guild The {@link Guild} specified.
     * @param currentNumber The current number for the {@link Guild}.
     * @return Whether or not setting the last number was successful.
     */
    @NotNull
    private Boolean setLastNumber(@NotNull Guild guild, @NotNull Integer currentNumber) {
        Connection connection = CafeBot.getSQLServer().getConnection();
        String arguments = "UPDATE cafeBot.counting_information SET last_number = (?) WHERE guild_id = (?);";

        try {
            PreparedStatement statement = connection.prepareStatement(arguments);
            statement.setInt(1, currentNumber);
            statement.setLong(2, Long.parseLong(guild.getId()));
            statement.execute();
            return true;
        } catch (SQLException e) {
            CafeBot.getLogManager().log(this.getClass(), LogLevel.WARN, "Error Setting Last Number: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Creates a new row for the Counting Information from the {@link Guild}.
     * @param guild The {@link Guild} specified.
     * @return Whether or not creating a new row was successful.
     */
    @NotNull
    public Boolean createNewRow(@NotNull Guild guild) {
        if (getHighestNumber(guild) != null) {
            return false;
        }

        Connection connection = CafeBot.getSQLServer().getConnection();
        String arguments = "INSERT IGNORE INTO cafeBot.counting_information (guild_id) VALUES (?);";

        try {
            PreparedStatement statement = connection.prepareStatement(arguments);
            statement.setLong(1, Long.parseLong(guild.getId()));
            statement.execute();
            return true;
        } catch (SQLException e) {
            CafeBot.getLogManager().log(this.getClass(), LogLevel.WARN, "Error Creating New Counting Row: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * The highest number from the {@link Guild}.
     * @param guild The {@link Guild} specified.
     * @return The highest number for the {@link Guild}.
     */
    @Nullable
    public Integer getHighestNumber(@NotNull Guild guild) {
        Connection connection = CafeBot.getSQLServer().getConnection();
        String arguments = "SELECT * FROM cafeBot.counting_information WHERE guild_id = (?);";

        try {
            PreparedStatement statement = connection.prepareStatement(arguments);
            statement.setLong(1, Long.parseLong(guild.getId()));

            ResultSet resultSet = statement.executeQuery();

            resultSet.next();
            return resultSet.getInt(2);
        } catch (SQLException e) {
            CafeBot.getLogManager().log(this.getClass(), LogLevel.WARN, "Error Getting Highest Number: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Gets the last number from the {@link Guild}.
     * @param guild The {@link Guild} specified.
     * @return The highest number for the {@link Guild}.
     */
    @Nullable
    public Integer getLastNumber(@NotNull Guild guild) {
        Connection connection = CafeBot.getSQLServer().getConnection();
        String arguments = "SELECT * FROM cafeBot.counting_information WHERE guild_id = (?);";

        try {
            PreparedStatement statement = connection.prepareStatement(arguments);
            statement.setLong(1, Long.parseLong(guild.getId()));

            ResultSet resultSet = statement.executeQuery();

            resultSet.next();
            return resultSet.getInt(3);
        } catch (SQLException e) {
            CafeBot.getLogManager().log(this.getClass(), LogLevel.WARN, "Error Getting Last Number: " + e.getMessage(), e);
            return null;
        }
    }

}
