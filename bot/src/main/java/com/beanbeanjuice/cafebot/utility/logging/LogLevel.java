package com.beanbeanjuice.cafebot.utility.logging;

import lombok.Getter;

import java.awt.*;

/**
 * A static class to be used with the {@link LogManager}.
 *
 * @author beanbeanjuice
 */
@Getter
public enum LogLevel {

    DEBUG(Color.blue, "TEST", "TEST", "https://cdn.beanbeanjuice.com/images/cafeBot/log_level/debug.png"),
    LOADING(Color.orange, "LOADING", "LOAD", "https://cdn.beanbeanjuice.com/images/cafeBot/log_level/loading.gif"),
    OKAY(Color.green, "OKAY", "SUCC", "https://cdn.beanbeanjuice.com/images/cafeBot/log_level/okay.gif"),
    ERROR(Color.red, "ERROR", "ERR", "https://cdn.beanbeanjuice.com/images/cafeBot/log_level/error.webp"),
    INFO(Color.cyan, "INFO", "INFO", "https://cdn.beanbeanjuice.com/images/cafeBot/log_level/info.gif"),
    WARN(Color.orange, "WARN", "WARN", "https://cdn.beanbeanjuice.com/images/cafeBot/log_level/warn.gif");

    private final Color color;
    private final String message;
    private final String code;
    private final String imageURL;

    /**
     * Creates a new instance of the {@link LogLevel} enum.
     * @param color The {@link Color} to be used.
     * @param message The code to be shown.
     * @param imageURL The url for the image.
     */
    LogLevel(Color color, String message, String code, String imageURL) {
        this.color = color;
        this.message = message;
        this.code = code;
        this.imageURL = imageURL;
    }

    /**
     * Gets the formatted code for the {@link LogLevel} chosen.
     * @return The code in its formatted state.
     */
    public String formatCode() {
        return code + (".").repeat(Math.max(0, 10 - code.length()));
    }

}
