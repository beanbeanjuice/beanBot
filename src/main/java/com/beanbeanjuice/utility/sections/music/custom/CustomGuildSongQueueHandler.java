package com.beanbeanjuice.utility.sections.music.custom;

import com.beanbeanjuice.CafeBot;
import com.beanbeanjuice.utility.sections.music.lavaplayer.PlayerManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A {@link CustomGuildSongQueueHandler} class for handling {@link CustomSong} in a {@link net.dv8tion.jda.api.entities.Guild Guild}.
 *
 * @author beanbeanjuice
 */
public class CustomGuildSongQueueHandler {

    private String guildID;
    private ArrayList<CustomSong> customSongQueue;
    private ArrayList<CustomSong> unshuffledQueue;
    private ArrayList<CustomSong> repeatQueue;
    private CustomSong currentSong;
    private boolean songPlaying = false;
    private boolean playlistRepeat = false;
    private boolean songRepeat = false;
    private boolean shuffle = false;

    /**
     * Creates a new {@link CustomGuildSongQueueHandler} object.
     * @param guildID The ID for that {@link net.dv8tion.jda.api.entities.Guild Guild}.
     */
    public CustomGuildSongQueueHandler(@NotNull String guildID) {
        this.guildID = guildID;
        customSongQueue = new ArrayList<>();
        unshuffledQueue = new ArrayList<>();
        repeatQueue = new ArrayList<>();
    }

    /**
     * Adds a custom song to the {@link ArrayList<CustomSong>}.
     * @param customSong The {@link CustomSong} to add.
     * @param fromRepeat Whether or not the {@link CustomSong} was added from the repeat mechanism.
     */
    public void addCustomSong(@NotNull CustomSong customSong, @NotNull Boolean fromRepeat) {

        // Adds the song randomly if needed.
        if (shuffle) {
            customSongQueue.add(CafeBot.getGeneralHelper().getRandomNumber(0, customSongQueue.size()), customSong);
        } else {
            customSongQueue.add(customSong);
        }
        unshuffledQueue.add(customSong);

        if (!fromRepeat && playlistRepeat) {
            repeatQueue.add(customSong);
        }
        queueNextSong();
    }

    /**
     * Queues up the next {@link CustomSong}.
     */
    public void queueNextSong() {
        if (!songPlaying) {
            try {
                songPlaying = true;
                currentSong = customSongQueue.remove(0);
                unshuffledQueue.remove(currentSong);
                PlayerManager.getInstance().loadAndPlay(currentSong.getSearchString(), CafeBot.getGuildHandler().getGuild(guildID));
            } catch (IndexOutOfBoundsException e) {
                if (playlistRepeat) {
                    songPlaying = false;
                    for (CustomSong customSong : repeatQueue) {
                        addCustomSong(customSong, true);
                    }
                } else {
                    currentSong = null;
                }
            }
        }

        if (currentSong == null && customSongQueue.isEmpty()) {
            songPlaying = false;
        }
    }

    /**
     * Clears everything in the {@link CustomGuildSongQueueHandler}.
     */
    public void clear() {
        customSongQueue.clear();
        repeatQueue.clear();
        unshuffledQueue.clear();
        playlistRepeat = false;
        songRepeat = false;
        shuffle = false;
        currentSong = null;
        songPlaying = false;
    }

    /**
     * Sets the shuffle state for the {@link CustomGuildSongQueueHandler}.
     * @param bool The {@link Boolean} to set the shuffle state to.
     */
    public void setShuffle(@NotNull Boolean bool) {
        shuffle = bool;

        if (shuffle) {
            unshuffledQueue = new ArrayList<>(customSongQueue);
            Collections.shuffle(customSongQueue);
        } else {
            customSongQueue = new ArrayList<>(unshuffledQueue);
            unshuffledQueue.clear();
        }
    }

    /**
     * @return The current shuffle state for the {@link CustomGuildSongQueueHandler}.
     */
    @NotNull
    public Boolean getShuffle() {
        return shuffle;
    }

    /**
     * Sets the playlist repeat state for the {@link CustomGuildSongQueueHandler}.
     * @param bool The {@link Boolean} to set the playlist repeat state to.
     */
    public void setPlaylistRepeating(@NotNull Boolean bool) {
        playlistRepeat = bool;

        if (playlistRepeat) {
            repeatQueue = new ArrayList<>(customSongQueue);
            repeatQueue.add(0, currentSong);
            songRepeat = false;
        } else {
            repeatQueue.clear();
        }
    }

    /**
     * @return The current playlist repeat state for the {@link CustomGuildSongQueueHandler}.
     */
    @NotNull
    public Boolean getPlaylistRepeating() {
        return playlistRepeat;
    }

    /**
     * @return The current song repeat state for the {@link CustomGuildSongQueueHandler}.
     */
    @NotNull
    public Boolean getSongRepeating() {
        return songRepeat;
    }

    /**
     * Sets the song repeat state for the {@link CustomGuildSongQueueHandler}.
     * @param bool The {@link Boolean} to set the song repeat state to.
     */
    public void setSongRepeating(@NotNull Boolean bool) {
        songRepeat = bool;

        if (songRepeat) {
            playlistRepeat = false;
            repeatQueue.clear();
        }
    }

    /**
     * @return The current {@link CustomSong} playing.
     */
    public CustomSong getCurrentSong() {
        return currentSong;
    }

    /**
     * @return The current {@link CustomSong} queue.
     */
    public ArrayList<CustomSong> getCustomSongQueue() {
        return customSongQueue;
    }

    /**
     * Sets the song playing state for the {@link CustomGuildSongQueueHandler}.
     * @param bool The {@link Boolean} to set the song playing state to.
     */
    public void setSongPlayingStatus(@NotNull Boolean bool) {
        songPlaying = bool;
    }

}