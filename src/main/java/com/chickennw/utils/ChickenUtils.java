package com.chickennw.utils;

import com.chickennw.utils.database.redis.RedisDatabase;
import com.chickennw.utils.listeners.packet.PacketListeners;
import com.chickennw.utils.logger.LoggerFactory;
import com.chickennw.utils.models.cross.PendingCrossLocationTeleport;
import com.chickennw.utils.models.cross.PendingCrossPlayerTeleport;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.tcoded.folialib.FoliaLib;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ChickenUtils {

    @Getter
    private static final Cache<UUID, PendingCrossPlayerTeleport> playerTeleportRequests = CacheBuilder.newBuilder()
        .expireAfterWrite(5, TimeUnit.SECONDS)
        .build();

    @Getter
    private static final Cache<UUID, PendingCrossLocationTeleport> locationTeleportRequests = CacheBuilder.newBuilder()
        .expireAfterWrite(5, TimeUnit.SECONDS)
        .build();

    @Getter
    private static JavaPlugin plugin;

    @Getter
    private static FoliaLib foliaLib;

    @Getter
    private static BukkitAudiences bukkitAudience;

    @Getter
    private static PacketListeners packetListeners;

    @Setter
    private static RedisDatabase redisDatabase;

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

        if (redisDatabase != null) {
            redisDatabase.close();
        }
    }
}
