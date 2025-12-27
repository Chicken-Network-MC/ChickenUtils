package com.chickennw.utils.managers;

import com.chickennw.utils.ChickenUtils;
import com.chickennw.utils.logger.LoggerFactory;
import com.chickennw.utils.models.addons.PluginAddon;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class AddonManager {

    private static final String ADDON_PATH = "addons";
    private static AddonManager instance;
    private final Logger logger = LoggerFactory.getLogger();
    private final ConcurrentHashMap<String, PluginAddon> addons = new ConcurrentHashMap<>();

    public static AddonManager getInstance() {
        if (instance == null) {
            instance = new AddonManager();
        }

        return instance;
    }

    private AddonManager() {

    }

    public void loadAddons() {
        unloadAddons();

        List<File> addonFiles = getAddonFiles();
        logger.info("Found {} addon files.", addonFiles.size());

        for (File file : addonFiles) {
            try {
                PluginAddon pluginAddon = loadAddonFromFile(file);
                if (pluginAddon != null) {
                    pluginAddon.onLoad();
                    pluginAddon.onEnable();
                    addons.put(pluginAddon.getAddonId(), pluginAddon);
                    logger.info("Loaded addon: {} v{}", pluginAddon.getAddonName(), pluginAddon.getAddonVersion());
                }
            } catch (Exception e) {
                logger.error("Failed to load addon from file: {}", file.getName(), e);
            }
        }
    }

    public void unloadAddons() {
        addons.forEach((name, pluginAddon) -> pluginAddon.onDisable());
        addons.clear();
    }

    private PluginAddon loadAddonFromFile(File jarFile) throws Exception {
        URL jarUrl = jarFile.toURI().toURL();
        PluginAddon pluginAddon = null;

        try (URLClassLoader classLoader = new URLClassLoader(new URL[]{jarUrl}, getClass().getClassLoader());
             JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                if (name.endsWith(".class")) {
                    String className = name.replace('/', '.').substring(0, name.length() - 6);
                    try {
                        Class<?> clazz = classLoader.loadClass(className);
                        if (PluginAddon.class.isAssignableFrom(clazz) && !clazz.equals(PluginAddon.class)) {
                            JavaPlugin plugin = ChickenUtils.getPlugin();
                            pluginAddon = (PluginAddon) clazz.getDeclaredConstructor(Plugin.class).newInstance(plugin);
                        }
                    } catch (NoClassDefFoundError | ClassNotFoundException ex) {
                        logger.info("Class not found or could not be loaded: {}", className, ex);
                    } catch (Exception e) {
                        logger.error("Failed to load addon from jar file: {}", name, e);
                    }
                }
            }
        }

        return pluginAddon;
    }

    private List<File> getAddonFiles() {
        Plugin plugin = ChickenUtils.getPlugin();
        File addonDir = new File(plugin.getDataFolder(), ADDON_PATH);
        logger.info("Addon directory: {}", addonDir.getAbsolutePath());
        if (!addonDir.exists()) {
            logger.info("Addon directory does not exist. Creating a new one.");
            boolean mkdirs = addonDir.mkdirs();
            if (!mkdirs) logger.error("Failed to create addon directory.");
        }

        List<File> addonFiles = new ArrayList<>();
        for (File file : Objects.requireNonNull(addonDir.listFiles())) {
            if (file.isFile() && file.getName().endsWith(".jar")) {
                logger.info("Found Addon file: {}", file.getAbsolutePath());
                addonFiles.add(file);
            }
        }

        return addonFiles;
    }
}
