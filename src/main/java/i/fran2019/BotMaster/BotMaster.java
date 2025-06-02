package i.fran2019.BotMaster;

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
import redis.clients.jedis.UnifiedJedis;

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

        this.mongoClient = this.configManager.MONGODB_ENABLED ? MongoClients.create(this.configManager.MONGODB_URI) : null;
        this.redisClient = this.configManager.REDIS_ENABLED ? new UnifiedJedis(this.configManager.REDIS_URI) : null;
        this.lavalinkClient = this.configManager.LAVALINK_ENABLED ? new LavalinkClient(Helpers.getUserIdFromToken(configManager.TOKEN)) : null;

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
        try {
            JDABuilder builder = JDABuilder.createDefault(configManager.TOKEN)
                    .enableIntents(GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS))
                    .enableCache(CacheFlag.getPrivileged())
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
}
