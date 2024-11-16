package com.beanbeanjuice.cafebot;

import com.beanbeanjuice.cafeapi.wrapper.endpoints.guilds.GuildsEndpoint;
import com.beanbeanjuice.cafebot.commands.cafe.BalanceCommand;
import com.beanbeanjuice.cafebot.commands.cafe.DonateCommand;
import com.beanbeanjuice.cafebot.commands.cafe.MenuCommand;
import com.beanbeanjuice.cafebot.commands.cafe.ServeCommand;
import com.beanbeanjuice.cafebot.commands.fun.*;
import com.beanbeanjuice.cafebot.commands.fun.birthday.BirthdayCommand;
import com.beanbeanjuice.cafebot.commands.fun.counting.CountingCommand;
import com.beanbeanjuice.cafebot.commands.fun.meme.MemeCommand;
import com.beanbeanjuice.cafebot.commands.fun.rate.RateCommand;
import com.beanbeanjuice.cafebot.commands.games.CoinFlipCommand;
import com.beanbeanjuice.cafebot.commands.games.DiceRollCommand;
import com.beanbeanjuice.cafebot.commands.games.TicTacToeCommand;
import com.beanbeanjuice.cafebot.commands.games.game.GameCommand;
import com.beanbeanjuice.cafebot.commands.generic.PingCommand;
import com.beanbeanjuice.cafebot.commands.generic.*;
import com.beanbeanjuice.cafebot.commands.interaction.*;
import com.beanbeanjuice.cafebot.commands.moderation.ClearChatCommand;
import com.beanbeanjuice.cafebot.commands.settings.AICommand;
import com.beanbeanjuice.cafebot.commands.settings.CustomChannelsCommand;
import com.beanbeanjuice.cafebot.commands.settings.daily.DailyCommand;
import com.beanbeanjuice.cafebot.commands.settings.goodbye.GoodbyeCommand;
import com.beanbeanjuice.cafebot.commands.settings.goodbye.message.GoodbyeMessageModalListener;
import com.beanbeanjuice.cafebot.commands.settings.update.UpdateCommand;
import com.beanbeanjuice.cafebot.commands.settings.welcome.WelcomeCommand;
import com.beanbeanjuice.cafebot.commands.settings.welcome.message.WelcomeMessageModalListener;
import com.beanbeanjuice.cafebot.commands.social.MemberCountCommand;
import com.beanbeanjuice.cafebot.commands.social.vent.VentCommand;
import com.beanbeanjuice.cafebot.commands.twitch.TwitchCommand;
import com.beanbeanjuice.cafebot.utility.EnvironmentVariable;
import com.beanbeanjuice.cafebot.utility.commands.CommandHandler;
import com.beanbeanjuice.cafebot.utility.helper.DailyChannelHelper;
import com.beanbeanjuice.cafebot.utility.helper.Helper;
import com.beanbeanjuice.cafebot.utility.helper.UpdateCheckHelper;
import com.beanbeanjuice.cafebot.utility.listeners.*;
import com.beanbeanjuice.cafebot.utility.listeners.ai.AIResponseListener;
import com.beanbeanjuice.cafebot.utility.logging.LogLevel;
import com.beanbeanjuice.cafebot.utility.logging.LogManager;
import com.beanbeanjuice.cafeapi.wrapper.CafeAPI;
import com.beanbeanjuice.cafeapi.wrapper.requests.RequestLocation;
import com.beanbeanjuice.cafebot.utility.sections.cafe.MenuHandler;
import com.beanbeanjuice.cafebot.utility.sections.fun.birthday.BirthdayHelper;
import com.beanbeanjuice.cafebot.utility.sections.game.TicTacToeListener;
import com.beanbeanjuice.cafebot.utility.sections.generic.HelpHandler;
import com.beanbeanjuice.cafebot.utility.sections.generic.HelpListener;
import com.beanbeanjuice.cafebot.utility.sections.twitch.TwitchHandler;
import com.sun.management.OperatingSystemMXBean;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.net.URIBuilder;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class CafeBot {

    // Bot Items
    @Getter private final String botVersion = getVersion();
    @Getter private final String botUserAgent = "java:com.beanbeanjuice.cafeBot:" + botVersion;
    @Getter private final ShardManager shardManager;

    // API
    @Getter private final CafeAPI cafeAPI;

    // Internal Items
    @Getter private final LogManager logger;

    // Listeners
    @Getter private AIResponseListener aiResponseListener;

    // Helpers
    private DailyChannelHelper dailyChannelHelper;
    @Getter private BirthdayHelper birthdayHelper;

    // Handlers
    private CommandHandler commandHandler;
    @Getter private MenuHandler menuHandler;
    @Getter private HelpHandler helpHandler;
    @Getter private TwitchHandler twitchHandler;

    // Additional Items
    @Getter private int commandsRun = 0;
    @Getter private final String discordAvatarUrl = "https://cdn.beanbeanjuice.com/images/cafeBot/cafeBot.gif";

    // TODO: Store mutual guilds in database.
    public CafeBot() throws InterruptedException {
        this.logger = new LogManager(
                this,
                "cafeBot Logging System",
                EnvironmentVariable.CAFEBOT_GUILD_ID.getSystemVariable(),
                EnvironmentVariable.CAFEBOT_GUILD_LOG_CHANNEL_ID.getSystemVariable(),
                "logs/"
        );
        this.cafeAPI = new CafeAPI(
                "beanbeanjuice",
                EnvironmentVariable.API_PASSWORD.getSystemVariable(),
                RequestLocation.valueOf(EnvironmentVariable.CAFEBOT_REQUEST_LOCATION.getSystemVariable())
        );
        this.cafeAPI.setKawaiiAPI(EnvironmentVariable.KAWAII_API_TOKEN.getSystemVariable());

        this.logger.addWebhookURL(EnvironmentVariable.CAFEBOT_GUILD_WEBHOOK_URL.getSystemVariable());
        this.logger.log(CafeBot.class, LogLevel.OKAY, "Starting bot!", true, false);

        this.shardManager = DefaultShardManagerBuilder
                .createDefault(EnvironmentVariable.CAFEBOT_TOKEN.getSystemVariable())
                .setActivity(Activity.playing("The barista is starting..."))
                .setStatus(OnlineStatus.IDLE)
                .enableIntents(
                        GatewayIntent.GUILD_EXPRESSIONS,
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT
                )
                .setMemberCachePolicy(MemberCachePolicy.ALL)  // ! - Needed for mutual guilds  // TODO: REMOVE?
                .setChunkingFilter(ChunkingFilter.ALL)  // ! - Needed for mutual guilds  // TODO: REMOVE?
                .build();

        logger.log(CafeBot.class, LogLevel.INFO, "Checking servers...");
        this.checkGuilds();

        this.logger.enableDiscordLogging();
        logger.log(CafeBot.class, LogLevel.INFO, "Enabled Discord Logging...", true, true);

        this.shardManager.setStatus(OnlineStatus.ONLINE);
        logger.log(CafeBot.class, LogLevel.OKAY, "The bot is online!");

        logger.log(CafeBot.class, LogLevel.INFO, "Adding commands...");
        setupCommands();
        this.menuHandler = new MenuHandler(this);

        setupListeners();

        this.startUpdateTimer();
        this.startBioUpdateTimer();
        this.startUptimeChecker();
    }

    private void startUptimeChecker() {
        CafeBot bot = this;
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    String urlString = String.format(
                            "%s?status=%s&msg=%s&ping=%d",
                            System.getenv("CAFEBOT_KUMA_URL"),
                            "up",
                            "OK",
                            (int) bot.getShardManager().getAverageGatewayPing()
                    );
                    URIBuilder uriBuilder = new URIBuilder(urlString);
                    SimpleHttpRequest httpRequest = new SimpleHttpRequest(Method.GET, uriBuilder.build());

                    CloseableHttpAsyncClient client = HttpAsyncClients.custom().build();
                    client.start();
                    client.execute(httpRequest, null).get();
                    client.close();
                } catch (Exception e) {
                    bot.getLogger().log(CafeBot.class, LogLevel.ERROR, "Failed to check uptime check.", e);
                }
            }
        };

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(task, 0, TimeUnit.SECONDS.toMillis(60));
    }

    // TODO: Does this only get it from cache?
    private void checkGuilds() {
        GuildsEndpoint guildsEndpoint = this.cafeAPI.getGuildsEndpoint();
        guildsEndpoint.getAllGuildInformation().thenAcceptAsync((information) -> {
            this.shardManager.getGuilds().forEach((guild) -> {
                if (information.containsKey(guild.getId())) return;

                guildsEndpoint.createGuildInformation(guild.getId());
            });
        });
    }

    private void setupCommands() {
        this.commandHandler = new CommandHandler(this);

        this.commandHandler.addCommands(
                // Generic
                new BotDonateCommand(this),
                new BotInviteCommand(this),
                new BotUpvoteCommand(this),
                new VersionCommand(this),
                new BugReportCommand(this),
                new FeatureCommand(this),
                new DefineCommand(this),
                new EmbedCommand(this),
                new GenerateCode(this),
                new HelpCommand(this),
                new PingCommand(this),
                new InfoCommand(this),
                new StatsCommand(this),
                new RemoveMyDataCommand(this),
                new SupportCommand(this),
                new WhoCommand(this),

                // Cafe
                new BalanceCommand(this),
                new DonateCommand(this),
                new ServeCommand(this),
                new MenuCommand(this),

                // Fun
                new AvatarCommand(this),
                new BannerCommand(this),
                new BirthdayCommand(this),
                new MemeCommand(this),
                new JokeCommand(this),
                new CountingCommand(this),
                new RateCommand(this),
                new EightBallCommand(this),

                // Games
                new CoinFlipCommand(this),
                new DiceRollCommand(this),
                new GameCommand(this),
                new TicTacToeCommand(this),

                // Interactions
                new AmazedCommand(this),
                new AskCommand(this),
                new BiteCommand(this),
                new BlushCommand(this),
                new BonkCommand(this),
                new BoopCommand(this),
                new CryCommand(this),
                new CuddleCommand(this),
                new DabCommand(this),
                new DanceCommand(this),
                new DieCommand(this),
                new GreetCommand(this),
                new HeadPatCommand(this),
                new HideCommand(this),
                new HmphCommand(this),
                new HugCommand(this),
                new KissCommand(this),
                new LickCommand(this),
                new LoveCommand(this),
                new NomCommand(this),
                new NoseBleedCommand(this),
                new OkCommand(this),
                new PokeCommand(this),
                new PoutCommand(this),
                new PunchCommand(this),
                new RageCommand(this),
                new ShootCommand(this),
                new ShushCommand(this),
                new SlapCommand(this),
                new SleepCommand(this),
                new SmileCommand(this),
                new StabCommand(this),
                new StareCommand(this),
                new ThrowCommand(this),
                new TickleCommand(this),
                new UWUCommand(this),
                new WaveCommand(this),
                new WinkCommand(this),
                new YellCommand(this),

                // Social
                new MemberCountCommand(this),
                new VentCommand(this),

                // Twitch
                new TwitchCommand(this),

                // Moderation
                new ClearChatCommand(this),

                // Settings
                new AICommand(this),
                new WelcomeCommand(this),
                new GoodbyeCommand(this),
                new UpdateCommand(this),
                new DailyCommand(this),
                new CustomChannelsCommand(this)
        );

        this.shardManager.addEventListener(commandHandler);
        this.helpHandler = new HelpHandler(commandHandler);
        this.twitchHandler = new TwitchHandler(EnvironmentVariable.CAFEBOT_TWITCH_ACCESS_TOKEN.getSystemVariable(), this);

        UpdateCheckHelper updateCheckHelper = new UpdateCheckHelper(this);
        updateCheckHelper.checkUpdate();

        this.dailyChannelHelper = new DailyChannelHelper(this);
        dailyChannelHelper.start();

        this.birthdayHelper = new BirthdayHelper(this);
        this.birthdayHelper.startBirthdayChecker();
    }

    public void addEventListener(final ListenerAdapter listener) {
        this.shardManager.addEventListener(listener);
    }

    private void setupListeners() {
        this.aiResponseListener = new AIResponseListener(this, EnvironmentVariable.CAFEBOT_OPENAI_API_KEY.getSystemVariable(), EnvironmentVariable.CAFEBOT_OPENAI_ASSISTANT_ID.getSystemVariable());
        this.shardManager.addEventListener(
                new BotAddListener(this),
                new BotRemoveListener(this),
                new CountingListener(this),
                new HelpListener(commandHandler, helpHandler),
                new TicTacToeListener(cafeAPI.getWinStreaksEndpoint()),
                aiResponseListener,
                new WelcomeListener(this),
                new GoodbyeListener(this),

                new WelcomeMessageModalListener(this.cafeAPI.getWelcomesEndpoint()),
                new GoodbyeMessageModalListener(this.cafeAPI.getGoodbyesEndpoint())
        );
    }

    private String getVersion() {
        String resourceName = "cafebot.properties"; // could also be a constant
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Properties props = new Properties();
        try (InputStream resourceStream = loader.getResourceAsStream(resourceName)) { props.load(resourceStream); }
        catch (IOException e) { throw new RuntimeException(e); }

        return "v" + props.get("version").toString();
    }

    private void startUpdateTimer() {
        CafeBot bot = this;

        Timer updateTimer = new Timer();
        TimerTask updateTimerTask = new TimerTask() {
            @Override
            public void run() {
                bot.getLogger().log(CafeBot.class, LogLevel.DEBUG, "Sending bot status message...", true, false);

                bot.getUser("690927484199370753").queue((owner) -> {
                    bot.pmUser(owner, getUpdateEmbed(bot.getShardManager().getAverageGatewayPing()));
                });
            }
        };
        updateTimer.scheduleAtFixedRate(updateTimerTask, 0, TimeUnit.DAYS.toMillis(1));
    }

    private void startBioUpdateTimer() {
        String initialString = "/help | " + this.botVersion + " - ";

        CafeBot bot = this;

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                int num = Helper.getRandomInteger(1, 4);
                String finalString = "";

                switch (num) {
                    case 1 -> finalString = "Currently in " + getTotalServers() + " cafés!";
                    case 2 -> finalString = "Waiting " + getTotalChannels() + " tables!";
                    case 3 -> finalString = "Serving " + getTotalUsers() + " customers!";
                }

                bot.getShardManager().setActivity(Activity.customStatus(initialString + finalString));
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0, TimeUnit.MINUTES.toMillis(1));
    }

    public MessageEmbed getUpdateEmbed(final double gatewayPing) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        double cpuLoad = (double) Math.round((ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class).getCpuLoad()*100) * 100) / 100;
        long systemMemoryTotal = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class).getTotalMemorySize()/1048576;
        long systemMemoryUsage = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class).getCommittedVirtualMemorySize()/1048576;
        long dedicatedMemoryTotal = Runtime.getRuntime().maxMemory()/1048576;
        long dedicatedMemoryUsage = Runtime.getRuntime().totalMemory()/1048576;

        embedBuilder.setTitle("Daily CafeBot Update");

        String description = String.format(
                """
                **__System Status__**: Online
                
                **__Current Version__** - `%s`
                **__Shards__** - `%d`
                **__Average Gateway Ping__** - `%.2f` ms
                **__CPU Usage__** - `%.2f%%`
                **__OS Memory Usage__** - `%d` mb / `%d` mb
                **__ Bot Memory Usage__** - `%d` mb / `%d` mb
                **__Bot Uptime__** - `%s`
                **__Commands Run (Since Restart)__** - `%d`
                """,
                this.botVersion,
                this.shardManager.getShardsTotal(),
                gatewayPing,
                cpuLoad,
                systemMemoryUsage,
                systemMemoryTotal,
                dedicatedMemoryUsage,
                dedicatedMemoryTotal,
                Helper.formatTimeDays(ManagementFactory.getRuntimeMXBean().getUptime()),
                this.commandsRun);

        embedBuilder.setDescription(description);
        embedBuilder.setThumbnail(this.discordAvatarUrl);
        embedBuilder.setColor(Color.green);
        return embedBuilder.build();
    }

    public int getTotalChannels() {
        return this.shardManager.getTextChannels().size();
    }

    public int getTotalServers() {
        return this.shardManager.getGuilds().size();
    }

    public int getTotalUsers() {
        int count = 0;
        for (Guild guild : this.shardManager.getGuilds()) count += guild.getMemberCount();
        return count;
    }

    private RestAction<User> getUser(String userID) {
        userID = userID.replace("<@!", "");
        userID = userID.replace("<@", ""); // Edge Case for Mobile
        userID = userID.replace(">", "");

        try {
            return this.shardManager.retrieveUserById(userID);
        } catch (NullPointerException | NumberFormatException e) {
            this.logger.log(CafeBot.class, LogLevel.ERROR, "Error getting user from cache: " + e.getMessage());
            throw e;
        }
    }

    public void pmUser(User user, MessageEmbed embed) {
        user.openPrivateChannel().flatMap(channel -> channel.sendMessageEmbeds(embed)).queue();
    }

    public void increaseCommandsRun() {
        this.commandsRun++;
    }

    public SelfUser getSelfUser() {
        return this.shardManager.getShards().getFirst().getSelfUser();
    }

    public static void main(String[] args) throws InterruptedException {
        new CafeBot();
    }

}
