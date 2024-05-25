package i.fran2019.BotMaster.API.implementations;

import i.fran2019.BotMaster.BotMaster;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Plugin {
    @Getter BotMaster botMaster;
    @Getter Logger logger;
    @Getter String name;
    @Getter String description;

    public Plugin(BotMaster botMaster, String name, String description) {
        this.botMaster = botMaster;
        this.name = name;
        this.description = description;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    public void onEnable() {

    }

    public void onDisable() {

    }
}
