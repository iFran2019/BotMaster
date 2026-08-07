package i.fran2019.BotMaster;

import ch.qos.logback.classic.Level;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import dev.arbjerg.lavalink.client.Helpers;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.libraries.jda.JDAVoiceUpdateListener;
import i.fran2019.BotMaster.Managers.CommandManager;
import i.fran2019.BotMaster.Managers.ConfigManager;
import i.fran2019.BotMaster.Managers.PluginManager;
import lombok.Getter;
import lombok.NonNull;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.RedisClient;
import redis.clients.jedis.UnifiedJedis;

import java.util.EnumSet;

public class BotMaster {
    @NonNull @Getter private static Logger logger = LoggerFactory.getLogger(BotMaster.class);
    @NonNull @Getter private static BotMaster botMaster;

    @Getter private CommandManager commandManager;
    @Getter private PluginManager pluginManager;
    @Getter private ConfigManager configManager;

    @Getter private MongoClient mongoClient;
    @Getter private UnifiedJedis redisClient;
    @Getter private LavalinkClient lavalinkClient;

    @NonNull @Getter private JDA jda;

    public static void main(String[] args) {
        botMaster = new BotMaster();
        botMaster.start();
    }

    private void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
        logger.info("Starting Bot");

        this.configManager = new ConfigManager();

        loadClients();

        botMaster.build();

        this.commandManager = new CommandManager();
        this.pluginManager = new PluginManager();
    }

    public void stop() {
        logger.info("Stopping Bot");
        if (this.pluginManager != null) this.pluginManager.disableAllPlugins();
        this.getJda().shutdownNow();
        try {
            this.getJda().awaitShutdown();
        } catch (InterruptedException e) {
            logger.error("Interrupted while shutting down JDA.", e);
            Thread.currentThread().interrupt();
        }
        if (this.mongoClient != null) {
            this.mongoClient.close();
        }
        this.configManager = null;
    }

    private void build() {
        logger.info("Building Bot");

        EnumSet<GatewayIntent> intents = EnumSet.noneOf(GatewayIntent.class);
        EnumSet<CacheFlag> cache = EnumSet.noneOf(CacheFlag.class);

        /* Privileged Intents Settings */
        if (configManager.PRESENCE_INTENT_ENABLED) intents.add(GatewayIntent.GUILD_PRESENCES);
        if (configManager.SERVER_MEMBERS_INTENT_ENABLED) intents.add(GatewayIntent.GUILD_MEMBERS);
        if (configManager.MESSAGE_CONTENT_INTENT_ENABLED) intents.add(GatewayIntent.MESSAGE_CONTENT);

        /* Cache - Auto Intents Settings */
        for (String cacheFlagName : configManager.CACHE_ENABLED) {
            try {
                CacheFlag cacheFlag = CacheFlag.valueOf(cacheFlagName.toUpperCase());
                cache.add(cacheFlag);
                GatewayIntent requiredIntent = cacheFlag.getRequiredIntent();
                if (requiredIntent != null) intents.add(requiredIntent);
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid cache flag: {}", cacheFlagName);
            }
        }

        try {
            JDABuilder builder = JDABuilder.createDefault(configManager.TOKEN)
                    .enableIntents(intents)
                    .enableCache(cache)
                    .setAutoReconnect(true);

            if (configManager.LAVALINK_ENABLED) builder.setVoiceDispatchInterceptor(new JDAVoiceUpdateListener(lavalinkClient));

            this.jda = builder.build().awaitReady();
        } catch (InvalidTokenException e) {
            logger.error("Invalid Token.", e);
        } catch (InterruptedException e) {
            logger.error("Interrupted while waiting for JDA to be ready.", e);
            Thread.currentThread().interrupt();
        }
    }

    private void loadClients() {
        // ═══════════════════════════════════════════════
        // ║                   DEBUG                     ║
        // ═══════════════════════════════════════════════

        ch.qos.logback.classic.Logger lgr = (ch.qos.logback.classic.Logger) LoggerFactory.getILoggerFactory().getLogger("ROOT");
        lgr.setLevel(configManager.DEBUG ? Level.DEBUG : Level.INFO);

        // ═══════════════════════════════════════════════
        // ║                  MONGODB                    ║
        // ═══════════════════════════════════════════════

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(this.configManager.MONGODB_URI))
                .build();
        this.mongoClient = MongoClients.create(settings);

        // ═══════════════════════════════════════════════
        // ║                   REDIS                     ║
        // ═══════════════════════════════════════════════

        this.redisClient = this.configManager.REDIS_ENABLED ? RedisClient.create(this.configManager.REDIS_URI) : null;

        // ═══════════════════════════════════════════════
        // ║                  LAVALINK                   ║
        // ═══════════════════════════════════════════════

        this.lavalinkClient = this.configManager.LAVALINK_ENABLED ? new LavalinkClient(Helpers.getUserIdFromToken(configManager.TOKEN)) : null;
    }
}
