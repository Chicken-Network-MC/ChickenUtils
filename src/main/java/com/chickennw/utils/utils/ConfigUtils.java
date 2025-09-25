package com.chickennw.utils.utils;

import com.chickennw.utils.managers.ConfigManager;
import com.chickennw.utils.models.config.ConfigClassHolder;
import eu.okaeri.configs.OkaeriConfig;

public class ConfigUtils {

    @SuppressWarnings("unchecked")
    public static <T extends OkaeriConfig> T get(Class<T> clazz) {
        ConfigManager configManager = ConfigManager.getInstance();
        ConfigClassHolder classHolddder = configManager.getConfigClassHolders().stream()
                .filter(holder -> holder.configClass().equals(clazz))
                .findFirst()
                .orElse(null);

        if (classHolddder == null) throw new RuntimeException("Class " + clazz.getName() + " not found!");
        return (T) classHolddder.config();
    }
}
