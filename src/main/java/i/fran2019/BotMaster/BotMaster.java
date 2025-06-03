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
import redis.clients.jedis.UnifiedJedis;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

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

    private void loadClients() {
        // ═══════════════════════════════════════════════
        // ║                   DEBUG                     ║
        // ═══════════════════════════════════════════════

        ch.qos.logback.classic.Logger lgr = (ch.qos.logback.classic.Logger) LoggerFactory.getILoggerFactory().getLogger("ROOT");
        lgr.setLevel(configManager.DEBUG ? Level.DEBUG : Level.INFO);

        // ═══════════════════════════════════════════════
        // ║                  MONGODB                    ║
        // ═══════════════════════════════════════════════

        SSLContext sct;
        try {
            sct = SSLContext.getInstance("TLSv1.2");
            sct.init(null, null, null);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
        SSLContext stcF = sct;

        String uri = this.configManager.MONGODB_URI;
        boolean isLocal = uri.contains("localhost") || uri.contains("127.0.0.1");
        boolean isSrv = uri.startsWith("mongodb+srv://");

        if (!isLocal && !uri.contains("tls=") && !isSrv) {
            if (!uri.contains("/")) uri += "/";
            if (!uri.contains("?")) uri += "?tls=true";
            else uri += "&tls=true";
        }

        this.mongoClient = this.configManager.MONGODB_ENABLED ? MongoClients.create(
                MongoClientSettings.builder()
                        .applyConnectionString(new ConnectionString(uri))
                        .applyToSslSettings(builder -> {
                            builder.enabled(!isLocal && !isSrv);
                            builder.context(!isLocal && !isSrv ? stcF : null);
                            builder.invalidHostNameAllowed(false);
                        })
                        .build()
        ) : null;

        // ═══════════════════════════════════════════════
        // ║                   REDIS                     ║
        // ═══════════════════════════════════════════════

        this.redisClient = this.configManager.REDIS_ENABLED ? new UnifiedJedis(this.configManager.REDIS_URI) : null;

        // ═══════════════════════════════════════════════
        // ║                  LAVALINK                   ║
        // ═══════════════════════════════════════════════

        this.lavalinkClient = this.configManager.LAVALINK_ENABLED ? new LavalinkClient(Helpers.getUserIdFromToken(configManager.TOKEN)) : null;
    }
}
