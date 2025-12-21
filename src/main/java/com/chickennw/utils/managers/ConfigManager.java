package com.chickennw.utils.managers;

import com.chickennw.utils.ChickenUtils;
import com.chickennw.utils.models.config.ConfigClassHolder;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import eu.okaeri.configs.yaml.bukkit.serdes.SerdesBukkit;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Getter
@Setter
@SuppressWarnings("unused")
public class ConfigManager {

    private static ConfigManager instance;
    private final List<ConfigClassHolder> configClassHolders = new ArrayList<>();

    public static synchronized ConfigManager getInstance() {
        if (instance == null) instance = new ConfigManager();

        return instance;
    }

    private ConfigManager() {

    }

    public void loadOkaeriConfig(Class<? extends OkaeriConfig> clazz, String folder) {
        JavaPlugin plugin = ChickenUtils.getPlugin();
        String fileName = clazz.getSimpleName().replace("File", "") + ".yml";
        File configFolder = folder == null ? new File(plugin.getDataFolder(), fileName) :
                new File(plugin.getDataFolder() + File.separator + folder, fileName);
        OkaeriConfig config = eu.okaeri.configs.ConfigManager.create(clazz, (it) -> {
            it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
            it.withBindFile(configFolder);
            it.withRemoveOrphans(true);
            it.saveDefaults();
            it.load(true);
        });

        ConfigClassHolder holder = new ConfigClassHolder(config, clazz);
        configClassHolders.add(holder);
        ChickenUtils.getPlugin().getSLF4JLogger().info("Loaded config file: {}", fileName);
    }

    public void loadOkaeriConfig(Class<? extends OkaeriConfig> clazz) {
        loadOkaeriConfig(clazz, null);
    }

    public void createFiles(String path) {
        JavaPlugin plugin = ChickenUtils.getPlugin();
        Logger logger = plugin.getLogger();
        File jarFile = new File(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());

        try {
            final JarFile jar = new JarFile(jarFile);
            final Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                final String name = entries.nextElement().getName();
                if (name.startsWith(path + "/") && !name.endsWith(path + "/")) {
                    File file = new File(plugin.getDataFolder(), name);
                    if (file.exists()) continue;

                    plugin.saveResource(name, false);
                    logger.info("Generated new file: " + name);
                }
            }
            jar.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private List<Class<? extends OkaeriConfig>> loadClasses() {
        Reflections reflections = new Reflections(ChickenUtils.getPlugin().getClass().getPackage());
        Set<Class<? extends OkaeriConfig>> set = reflections.getSubTypesOf(OkaeriConfig.class);

        return set.stream()
                .filter(clazz -> !Modifier.isStatic(clazz.getModifiers()))
                .collect(Collectors.toList());
    }
}
