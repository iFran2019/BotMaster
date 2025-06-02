package i.fran2019.BotMaster.Utils;

import i.fran2019.BotMaster.BotMaster;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PluginClassLoader extends URLClassLoader {
    public PluginClassLoader(URL url) {
        super(new URL[]{url}, BotMaster.class.getClassLoader());
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> loadedClass = findLoadedClass(name);
            if (loadedClass == null) {
                try {
                    loadedClass = getParent().loadClass(name);
                } catch (ClassNotFoundException e) {
                    try {
                        loadedClass = findClass(name);
                    } catch (IllegalAccessError ignore) {}
                }
            }
            return loadedClass;
        }
    }

    public void preloadClasses(JarFile jarFile, PluginClassLoader pluginClassLoader) {
        Enumeration<JarEntry> entries = jarFile.entries();

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();

            if (entry.getName().endsWith(".class") && !entry.isDirectory()) {
                String className = entry.getName()
                        .replace("/", ".")
                        .replace(".class", "");

                try {
                    pluginClassLoader.loadClass(className);
                } catch (ClassNotFoundException | NoClassDefFoundError ignore) {}
            }
        }
    }
}