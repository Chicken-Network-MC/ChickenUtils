package com.chickennw.utils.cross;

import com.chickennw.utils.ChickenUtils;
import com.chickennw.utils.database.redis.RedisDatabase;
import com.chickennw.utils.listeners.bukkit.CrossServerTeleportListeners;
import com.chickennw.utils.models.config.redis.RedisConfiguration;
import com.chickennw.utils.models.redis.RedisMessage;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Getter
@SuppressWarnings("unused")
public class CrossTeleportManager {

    public static final String TELEPORT_SERVER_KEY = "cross-teleport-server";

    private final Cache<UUID, PendingCrossPlayerTeleport> playerTeleportRequests = CacheBuilder.newBuilder()
        .expireAfterWrite(5, TimeUnit.SECONDS)
        .build();
    private final Cache<UUID, PendingCrossLocationTeleport> locationTeleportRequests = CacheBuilder.newBuilder()
        .expireAfterWrite(5, TimeUnit.SECONDS)
        .build();

    private final String serverName;
    private final RedisDatabase redisDatabase;
    private final RedisConfiguration redisConfiguration;

    public CrossTeleportManager(String serverName, RedisDatabase redisDatabase, RedisConfiguration redisConfiguration) {
        this.serverName = serverName;
        this.redisDatabase = redisDatabase;
        this.redisConfiguration = redisConfiguration;

        JavaPlugin plugin = ChickenUtils.getPlugin();
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
        plugin.getServer().getPluginManager().registerEvents(new CrossServerTeleportListeners(this), plugin);
    }

    public void teleportLocation(PendingCrossLocationTeleport location) {
        if (location.server().equalsIgnoreCase(serverName)) {
            Location bukkitLocation = new Location(Bukkit.getWorld(location.world()), location.x(), location.y(), location.z(),
                location.yaw(), location.pitch());

            Player player = Bukkit.getPlayer(location.player());
            if (player == null) throw new NullPointerException("Player is null");

            player.teleportAsync(bukkitLocation);
        } else {
            locationTeleportRequests.put(location.player(), location);

            JSONObject json = new JSONObject();
            json.put("method", "cross-server-teleport-location");
            json.put("player", location.player().toString());
            json.put("world", location.world());
            json.put("targetServer", location.server());
            json.put("x", location.x());
            json.put("y", location.y());
            json.put("z", location.z());
            json.put("yaw", location.yaw());
            json.put("pitch", location.pitch());

            RedisMessage redisMessage = new RedisMessage(redisConfiguration.getChannel(), json.toString());
            redisDatabase.publish(redisMessage);

            Player player = Bukkit.getPlayer(location.player());
            if (player == null) throw new NullPointerException("Player is null");

            move(location.server(), player);
        }
    }

    public void teleportToPlayer(PendingCrossPlayerTeleport pendingCrossPlayerTeleport) {
        Player targetPlayer = Bukkit.getPlayer(pendingCrossPlayerTeleport.target());
        Player bukkitPlayer = Bukkit.getPlayer(pendingCrossPlayerTeleport.player());
        if (targetPlayer != null && bukkitPlayer != null) {
            bukkitPlayer.teleportAsync(targetPlayer.getLocation());
            return;
        }

        JSONObject json = new JSONObject();
        json.put("method", "cross-server-teleport-player");
        json.put("player", pendingCrossPlayerTeleport.player().toString());
        json.put("targetServer", pendingCrossPlayerTeleport.server());
        json.put("target", pendingCrossPlayerTeleport.target().toString());

        RedisMessage redisMessage = new RedisMessage(redisConfiguration.getChannel(), json.toString());
        redisDatabase.publish(redisMessage);

        if (bukkitPlayer == null) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("method", CrossTeleportManager.TELEPORT_SERVER_KEY);
            jsonObject.put("uuid", pendingCrossPlayerTeleport.player().toString());
            jsonObject.put("server", pendingCrossPlayerTeleport.server());

            RedisMessage teleportServer = new RedisMessage(redisConfiguration.getChannel(), jsonObject.toString());
            redisDatabase.publish(teleportServer);
        } else {
            playerTeleportRequests.put(pendingCrossPlayerTeleport.player(), pendingCrossPlayerTeleport);
            move(pendingCrossPlayerTeleport.server(), bukkitPlayer);
        }
    }

    private void move(String serverName, Player player) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(serverName);
        player.sendPluginMessage(ChickenUtils.getPlugin(), "BungeeCord", out.toByteArray());
    }
}
