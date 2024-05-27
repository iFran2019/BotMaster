package i.fran2019.BotMaster.Managers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.List;
import java.util.Map;

public class ConfigManager {
    private final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    private Map<String, Object> configMap;

    public String TOKEN;
    public Boolean MONGODB_ENABLED;
    public String MONGODB_URI;
    public Boolean COMMANDS_SLASH_ENABLED;
    public String COMMANDS_SLASH_REGISTER;
    public List<String> COMMANDS_DISABLED;

    public ConfigManager() {
        reloadConfig();
    }

    public void reloadConfig(){
        loadConfigMap();
        setConfigValues();
    }

    private void setConfigValues(){
        TOKEN = (String) getConfigValue("token");
        MONGODB_ENABLED = (Boolean) getConfigValue("mongodb.enabled");
        MONGODB_URI = (String) getConfigValue("mongodb.uri");
        COMMANDS_SLASH_ENABLED = (Boolean) getConfigValue("commands.slash.enabled");
        COMMANDS_SLASH_REGISTER = (String) getConfigValue("commands.slash.register");
        COMMANDS_DISABLED = (List<String>) getConfigValue("commands.disabled");
    }

    private void loadConfigMap() {
        try {
            File configFile = new File("config.yml");
            if (!configFile.exists()) {
                createDefaultConfigFile(configFile);
                loadConfigMap();
            }
            Yaml yaml = new Yaml();
            try (FileInputStream fis = new FileInputStream(configFile)) {
                configMap = yaml.load(fis);
                if (configMap == null) {
                    logger.error("Config file format is invalid");
                    configMap = null;
                }
            }
        } catch (IOException e) {
            logger.error("Error loading the config", e);
        }
    }

    private Object getConfigValue(String path) {
        if (configMap == null) {
            return null;
        }
        String[] keys = path.split("\\.");
        Map<String, Object> currentMap = configMap;
        for (int i = 0; i < keys.length - 1; i++) {
            Object value = currentMap.get(keys[i]);
            if (!(value instanceof Map)) {
                return null;
            }
            currentMap = (Map<String, Object>) value;
        }
        return currentMap.get(keys[keys.length - 1]);
    }

    private void createDefaultConfigFile(File configFile) {
        InputStream defaultConfigStream = getClass().getClassLoader().getResourceAsStream("config.yml");
        if (defaultConfigStream == null) {
            logger.error("Default config file not found in resources");
            return;
        }

        try (OutputStream outputStream = new FileOutputStream(configFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = defaultConfigStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            logger.info("Default config file created");
        } catch (IOException e) {
            logger.error("Error creating default config file", e);
        }
    }
}