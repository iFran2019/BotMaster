package i.fran2019.BotMaster.API.Managers;

import i.fran2019.BotMaster.API.implementations.Plugin;
import i.fran2019.BotMaster.BotMaster;

import java.io.File;
import java.io.IOException;

public class ConfigManager {
    private final Plugin plugin;
    private File configDir;

    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
        createDirectory();
    }

    public File configFile(String fileName) {
        File configFile = new File(configDir, fileName);

        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                BotMaster.getLogger().error("IOException: {}", e.toString());
            }
        }

        return configFile;
    }

    private void createDirectory() {
        configDir = new File(BotMaster.getBotMaster().getPluginManager().getPluginFolder(), plugin.getName());

        if (!configDir.exists()) {
            try {
                configDir.mkdirs();
            } catch (SecurityException e) {
                BotMaster.getLogger().error("SecurityException: {}", e.toString());
            }
        }
    }
}
