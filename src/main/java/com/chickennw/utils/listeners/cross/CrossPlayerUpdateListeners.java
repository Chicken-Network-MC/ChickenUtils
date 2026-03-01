package com.chickennw.utils.listeners.cross;

import com.chickennw.utils.database.redis.CrossServerRedisDatabase;
import com.chickennw.utils.models.cross.CrossServerPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CrossPlayerUpdateListeners implements Listener {

    private final CrossServerRedisDatabase redisDatabase;
    private final ConcurrentHashMap<UUID, CrossServerPlayer> crossServerPlayers;

    public CrossPlayerUpdateListeners(CrossServerRedisDatabase redisDatabase, ConcurrentHashMap<UUID, CrossServerPlayer> crossServerPlayers) {
        this.redisDatabase = redisDatabase;
        this.crossServerPlayers = crossServerPlayers;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        UUID uuid = player.getUniqueId();
        String name = player.getName();
        String serverName = redisDatabase.getRedisConfiguration().getServer();
        String worldName = player.getWorld().getName();

        CrossServerPlayer crossServerPlayer = new CrossServerPlayer(uuid, name, serverName, worldName);
        crossServerPlayers.put(uuid, crossServerPlayer);
        redisDatabase.publishCrossPlayerUpdate(crossServerPlayer);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        crossServerPlayers.remove(uuid);
        redisDatabase.publishCrossPlayerRemove(uuid);
    }

    @EventHandler
    public void onChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        CrossServerPlayer crossServerPlayer = crossServerPlayers.get(player.getUniqueId());
        crossServerPlayer.setWorld(player.getWorld().getName());
        redisDatabase.publishCrossPlayerUpdate(crossServerPlayer);
    }
}
