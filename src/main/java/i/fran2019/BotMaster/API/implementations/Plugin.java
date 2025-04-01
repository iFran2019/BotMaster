package i.fran2019.BotMaster.API.implementations;

import i.fran2019.BotMaster.API.Managers.ConfigManager;
import i.fran2019.BotMaster.BotMaster;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Plugin {
    @Getter BotMaster botMaster;
    @Getter ConfigManager configManager;
    @Getter Logger logger;
    @Getter String name;
    @Getter String description;
    @Getter String version;

    public Plugin(BotMaster botMaster, String name, String description, String version) {
        this.botMaster = botMaster;
        this.name = name;
        this.description = description;
        this.version = version;

        this.configManager = new ConfigManager(this);
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    public void onEnable() {

    }

    public void onDisable() {

    }
}