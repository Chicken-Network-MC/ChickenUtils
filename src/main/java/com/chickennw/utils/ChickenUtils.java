package com.chickennw.utils;

import com.chickennw.utils.listeners.PacketListeners;
import com.chickennw.utils.logger.LoggerFactory;
import com.tcoded.folialib.FoliaLib;
import lombok.Getter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

public class ChickenUtils {

    @Getter
    private static JavaPlugin plugin;

    @Getter
    private static FoliaLib foliaLib;

    @Getter
    private static BukkitAudiences bukkitAudience;

    @Getter
    private static PacketListeners packetListeners;

    public static void setPlugin(JavaPlugin plugin) {
        ChickenUtils.plugin = plugin;
        foliaLib = new FoliaLib(plugin);
        bukkitAudience = BukkitAudiences.create(plugin);

        Logger logger = LoggerFactory.getLogger();
        if (plugin.getServer().getPluginManager().isPluginEnabled("PacketEvents")) {
            packetListeners = new PacketListeners();
            logger.info("PacketEvents detected and hooked.");
        } else {
            logger.info("PacketEvents not detected, skipping hook.");
        }
    }

    public static void disable() {
        if (bukkitAudience != null) {
            bukkitAudience.close();
        }

        if (plugin.getServer().getPluginManager().isPluginEnabled("PacketEvents")) {
            packetListeners.disable();
        }
    }
}
