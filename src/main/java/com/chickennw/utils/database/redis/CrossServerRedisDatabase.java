package com.chickennw.utils.database.redis;

import com.chickennw.utils.ChickenUtils;
import com.chickennw.utils.listeners.cross.CrossPlayerUpdateListeners;
import com.chickennw.utils.listeners.cross.CrossServerTeleportListeners;
import com.chickennw.utils.models.config.redis.RedisConfiguration;
import com.chickennw.utils.models.cross.CrossServerPlayer;
import com.chickennw.utils.models.redis.RedisMessage;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public abstract class CrossServerRedisDatabase extends RedisDatabase {

    public static final String PLAYER_UPDATE_KEY = "cross-player-update";
    public static final String PLAYER_REMOVE_KEY = "cross-player-remove";
    private final RedisConfiguration redisConfiguration;
    private final ConcurrentHashMap<UUID, CrossServerPlayer> crossServerPlayers;

    public CrossServerRedisDatabase(RedisConfiguration redisConfiguration,
                                    ConcurrentHashMap<UUID, CrossServerPlayer> crossServerPlayers) {
        super(redisConfiguration);
        this.redisConfiguration = redisConfiguration;
        this.crossServerPlayers = crossServerPlayers;
        JavaPlugin plugin = ChickenUtils.getPlugin();

        CrossPlayerUpdateListeners crossPlayerUpdateListeners = new CrossPlayerUpdateListeners(this, crossServerPlayers);
        plugin.getServer().getPluginManager().registerEvents(crossPlayerUpdateListeners, plugin);

        CrossServerTeleportListeners crossTeleportListeners = new CrossServerTeleportListeners();
        plugin.getServer().getPluginManager().registerEvents(crossTeleportListeners, plugin);
    }

    public void publishCrossPlayerUpdate(CrossServerPlayer crossServerPlayer) {
        JSONObject json = new JSONObject();
        json.put("method", PLAYER_UPDATE_KEY);
        json.put("uuid", crossServerPlayer.getUuid().toString());
        json.put("name", crossServerPlayer.getName());
        json.put("server", crossServerPlayer.getServer());
        json.put("world", crossServerPlayer.getWorld());

        RedisMessage redisMessage = new RedisMessage(redisConfiguration.getChannel(), json);
        publish(redisMessage);
    }

    public void publishCrossPlayerRemove(UUID uuid) {
        JSONObject json = new JSONObject();
        json.put("method", PLAYER_REMOVE_KEY);
        json.put("uuid", uuid.toString());

        RedisMessage redisMessage = new RedisMessage(redisConfiguration.getChannel(), json);
        publish(redisMessage);
    }

    public void onCrossPlayerUpdate(String message) {
        try {
            JSONObject json = new JSONObject(message);
            String method = json.getString("method");
            if (method.equals(PLAYER_UPDATE_KEY)) {
                String uuidString = json.getString("uuid");
                UUID uuid = UUID.fromString(uuidString);
                String name = json.getString("name");
                String server = json.getString("server");
                String world = json.getString("world");

                CrossServerPlayer crossServerPlayer = new CrossServerPlayer(uuid, name, server, world);
                crossServerPlayers.put(uuid, crossServerPlayer);
            } else if (method.equals(PLAYER_REMOVE_KEY)) {
                String uuidString = json.getString("uuid");
                UUID uuid = UUID.fromString(uuidString);

                crossServerPlayers.remove(uuid);
            }
        } catch (Exception ex) {
            logger.error("Error on cross player update: {}", ex.getMessage(), ex);
        }
    }
}