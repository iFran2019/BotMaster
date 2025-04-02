package i.fran2019.BotMaster.Managers;

import i.fran2019.BotMaster.API.implementations.Plugin;
import i.fran2019.BotMaster.BotMaster;
import i.fran2019.BotMaster.Utils.PluginClassLoader;
import lombok.Getter;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class PluginManager {
    @Getter private List<Plugin> plugins;

    public PluginManager() {
        File pluginFolder = new File("plugins");
        if (!pluginFolder.exists()) {
            if (pluginFolder.mkdir()) {
                BotMaster.getLogger().info("The 'plugins' folder has been created.");
            } else {
                BotMaster.getLogger().info("Could not create 'plugins' folder.");
                return;
            }
        }
        File[] pluginFiles = pluginFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));

        if (pluginFiles == null) {
            BotMaster.getLogger().info("There is no plugin loaded.");
            return;
        }

        if (plugins == null) {
            plugins = new ArrayList<>();
        }

        for (File pluginFile : pluginFiles) {
            loadPlugin(pluginFile);
        }

        Thread thread = new Thread(() -> BotMaster.getBotMaster().getCommandManager().registerSlashCommands());
        thread.setName("CommandLoader");
        thread.start();
    }

    public void disableAllPlugins() {
        for (Plugin plugin : plugins) plugin.onDisable();
    }

    public void loadPlugin(File pluginFile) {
        try (JarFile jarFile = new JarFile(pluginFile)) {
            ZipEntry entry = jarFile.getEntry("bot.yml");
            if (entry != null) {
                InputStream inputStream = jarFile.getInputStream(entry);
                Yaml yaml = new Yaml();
                Map<String, String> config = yaml.load(inputStream);
                try (PluginClassLoader pluginClassLoader = new PluginClassLoader(pluginFile.toURI().toURL())) {
                    Class<?> pluginClass = pluginClassLoader.loadClass(config.get("main"));
                    pluginClassLoader.preloadClasses(jarFile, pluginClassLoader);
                    if (Plugin.class.isAssignableFrom(pluginClass)) {
                        Constructor<?> constructor = pluginClass.getDeclaredConstructor(BotMaster.class, String.class, String.class, String.class);
                        Plugin pluginInstance = (Plugin) constructor.newInstance(BotMaster.getBotMaster(), config.get("name"), config.get("description"), config.get("version"));
                        pluginInstance.onEnable();
                        plugins.add(pluginInstance);
                    } else {
                        BotMaster.getLogger().warn("Class in plugin file '{}' is not a subclass of Plugin.", pluginFile.getName());
                    }
                }
            } else {
                BotMaster.getLogger().warn("Plugin file '{}' does not contain a bot.yml file.", pluginFile.getName());
            }
        } catch (IOException | NullPointerException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            BotMaster.getLogger().warn("Error loading plugin file '{}': {} {}", pluginFile.getName(), e.getClass(), e.getMessage());
            e.printStackTrace();
        }
    }
}
