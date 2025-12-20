package com.chickennw.utils;

import com.tcoded.folialib.FoliaLib;
import lombok.Getter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;

public class ChickenUtils {

    @Getter
    private static JavaPlugin plugin;

    @Getter
    private static FoliaLib foliaLib;

    @Getter
    private static BukkitAudiences bukkitAudience;

    public static void setPlugin(JavaPlugin plugin) {
        ChickenUtils.plugin = plugin;
        foliaLib = new FoliaLib(plugin);
        bukkitAudience = BukkitAudiences.create(plugin);
    }
}
