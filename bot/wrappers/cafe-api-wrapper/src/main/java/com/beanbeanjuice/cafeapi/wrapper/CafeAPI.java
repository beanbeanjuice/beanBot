package com.beanbeanjuice.cafeapi.wrapper;

import com.beanbeanjuice.cafeapi.wrapper.endpoints.CafeEndpoint;
import com.beanbeanjuice.cafeapi.wrapper.endpoints.mutualguilds.MutualGuildsEndpoint;
import com.beanbeanjuice.kawaiiapi.wrapper.KawaiiAPI;
import com.beanbeanjuice.cafeapi.wrapper.endpoints.goodbyes.GoodbyesEndpoint;
import com.beanbeanjuice.cafeapi.wrapper.requests.*;
import com.beanbeanjuice.cafeapi.wrapper.user.UsersEndpoint;
import com.beanbeanjuice.cafeapi.wrapper.endpoints.beancoins.users.DonationUsersEndpoint;
import com.beanbeanjuice.cafeapi.wrapper.endpoints.birthdays.BirthdaysEndpoint;
import com.beanbeanjuice.cafeapi.wrapper.endpoints.cafe.CafeUsersEndpoint;
import com.beanbeanjuice.cafeapi.wrapper.endpoints.codes.GeneratedCodesEndpoint;
import com.beanbeanjuice.cafeapi.wrapper.endpoints.counting.CountingEndpoint;
import com.beanbeanjuice.cafeapi.wrapper.endpoints.guilds.GuildsEndpoint;
import com.beanbeanjuice.cafeapi.wrapper.endpoints.interactions.InteractionsEndpoint;
import com.beanbeanjuice.cafeapi.wrapper.endpoints.interactions.pictures.InteractionPicturesEndpoint;
import com.beanbeanjuice.cafeapi.wrapper.endpoints.minigames.winstreaks.WinStreaksEndpoint;
import com.beanbeanjuice.cafeapi.wrapper.endpoints.polls.PollsEndpoint;
import com.beanbeanjuice.cafeapi.wrapper.endpoints.raffles.RafflesEndpoint;
import com.beanbeanjuice.cafeapi.wrapper.endpoints.twitches.TwitchEndpoint;
import com.beanbeanjuice.cafeapi.wrapper.endpoints.version.VersionsEndpoint;
import com.beanbeanjuice.cafeapi.wrapper.endpoints.voicebinds.VoiceChannelBindsEndpoint;
import com.beanbeanjuice.cafeapi.wrapper.endpoints.welcomes.WelcomesEndpoint;
import com.beanbeanjuice.cafeapi.wrapper.endpoints.words.WordsEndpoint;
import lombok.Getter;

import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CafeAPI {

    private String apiKey;
    private String userAgent;
    @Getter private static RequestLocation requestLocation;

    @Getter private KawaiiAPI kawaiiAPI;

    @Getter private final UsersEndpoint usersEndpoint;

    @Getter private final WordsEndpoint wordsEndpoint;
    @Getter private final WelcomesEndpoint welcomesEndpoint;
    @Getter private final GoodbyesEndpoint goodbyesEndpoint;
    @Getter private final VoiceChannelBindsEndpoint voiceChannelBindsEndpoint;
    @Getter private final RafflesEndpoint rafflesEndpoint;
    @Getter private final PollsEndpoint pollsEndpoint;
    @Getter private final WinStreaksEndpoint winStreaksEndpoint;
    @Getter private final MutualGuildsEndpoint mutualGuildsEndpoint;
    @Getter private final InteractionsEndpoint interactionsEndpoint;
    @Getter private final TwitchEndpoint twitchEndpoint;
    @Getter private final GuildsEndpoint guildsEndpoint;
    @Getter private final GeneratedCodesEndpoint generatedCodesEndpoint;
    @Getter private final VersionsEndpoint versionsEndpoint;
    @Getter private final CountingEndpoint countingEndpoint;
    @Getter private final CafeUsersEndpoint cafeUsersEndpoint;
    @Getter private final BirthdaysEndpoint birthdaysEndpoint;
    @Getter private final DonationUsersEndpoint donationUsersEndpoint;
    @Getter private final InteractionPicturesEndpoint interactionPicturesEndpoint;

    /**
     * Creates a new {@link CafeAPI} object.
     * @param username The {@link String username}.
     * @param password The {@link String password}.
     * @param requestLocation The {@link RequestLocation requestLocation}.
     */
    public CafeAPI(String username, String password, RequestLocation requestLocation) {
        this.userAgent = username;
        CafeAPI.requestLocation = requestLocation;

        setAPIKey(username, password);
        startRefreshTimer(username, password);

        usersEndpoint = new UsersEndpoint();

        // cafeBot
        wordsEndpoint = new WordsEndpoint();
        welcomesEndpoint = new WelcomesEndpoint();
        goodbyesEndpoint = new GoodbyesEndpoint();
        voiceChannelBindsEndpoint = new VoiceChannelBindsEndpoint();
        rafflesEndpoint = new RafflesEndpoint();
        pollsEndpoint = new PollsEndpoint();
        winStreaksEndpoint = new WinStreaksEndpoint();
        mutualGuildsEndpoint = new MutualGuildsEndpoint();
        interactionsEndpoint = new InteractionsEndpoint();
        twitchEndpoint = new TwitchEndpoint();
        guildsEndpoint = new GuildsEndpoint();
        generatedCodesEndpoint = new GeneratedCodesEndpoint();
        versionsEndpoint = new VersionsEndpoint();
        countingEndpoint = new CountingEndpoint();
        cafeUsersEndpoint = new CafeUsersEndpoint();
        birthdaysEndpoint = new BirthdaysEndpoint();
        donationUsersEndpoint = new DonationUsersEndpoint();
        interactionPicturesEndpoint = new InteractionPicturesEndpoint(this);

        kawaiiAPI = new KawaiiAPI("anonymous");
    }

    private void setAPIKey(final String username, final String password) {
        try {
            apiKey = getToken(username, password);
        } catch (Exception e) {
            Logger.getLogger(CafeAPI.class.getName()).log(Level.SEVERE, "Unable to login. Could the username or password be incorrect?");
        }
        CafeEndpoint.updateAPIKey(apiKey);
    }

    private void startRefreshTimer(String username, String password) {
        Logger.getLogger(CafeAPI.class.getName()).log(Level.INFO, "Starting the API key refresh timer.");
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                setAPIKey(username, password);
                Logger.getLogger(CafeAPI.class.getName()).log(Level.INFO, "Renewed the API key.");
            }
        };

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, TimeUnit.MINUTES.toMillis(55), TimeUnit.MINUTES.toMillis(55));
    }

    /**
     * Sets the {@link KawaiiAPI} token.
     * @param token The {@link String} token for the {@link KawaiiAPI}.
     */
    public void setKawaiiAPI(String token) {
        kawaiiAPI = new KawaiiAPI(token);
    }

    private String getToken(String username, String password) {
        Request request = RequestBuilder.create(RequestRoute.CAFE, RequestType.POST)
                .setRoute("/user/login")
                .addParameter("username", username)
                .addParameter("password", password)
                .build();

        return request.getData().get("access_token").textValue();
    }

}
