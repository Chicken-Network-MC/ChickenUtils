package com.chickennw.utils.listeners.bukkit;

import com.chickennw.utils.ChickenUtils;
import com.chickennw.utils.cross.CrossTeleportManager;
import com.chickennw.utils.cross.PendingCrossLocationTeleport;
import com.chickennw.utils.cross.PendingCrossPlayerTeleport;
import com.chickennw.utils.logger.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.slf4j.Logger;

import java.util.UUID;

@RequiredArgsConstructor
public class CrossServerTeleportListeners implements Listener {

    private final Logger logger = LoggerFactory.getLogger();
    private final CrossTeleportManager crossTeleportManager;

    @EventHandler
    public void onPlayerJoinPlayerTeleport(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        PendingCrossPlayerTeleport request = crossTeleportManager.getPlayerTeleportRequests().getIfPresent(player.getUniqueId());
        if (request != null) {
            UUID target = request.target();
            Player targetPlayer = Bukkit.getPlayer(target);
            if (targetPlayer == null) return;

            teleport(player, targetPlayer.getLocation());
        }
    }

    @EventHandler
    public void onPlayerJoinLocationTeleport(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        PendingCrossLocationTeleport request = crossTeleportManager.getLocationTeleportRequests().getIfPresent(player.getUniqueId());
        if (request != null) {
            String world = request.world();
            double x = request.x();
            double y = request.y();
            double z = request.z();
            float yaw = request.yaw();
            float pitch = request.pitch();

            World targetWorld = Bukkit.getWorld(world);
            if (targetWorld == null) {
                logger.error("Got null world on request: {}", request);
                return;
            }

            Location targetLocation = new Location(targetWorld, x, y, z, yaw, pitch);
            teleport(player, targetLocation);
        }
    }

    private void teleport(Player player, Location location) {
        ChickenUtils.getFoliaLib().getScheduler().runAtEntityLater(player, (task) -> {
            player.teleportAsync(location);
        }, 5);

        crossTeleportManager.getLocationTeleportRequests().invalidate(player.getUniqueId());
        crossTeleportManager.getPlayerTeleportRequests().invalidate(player.getUniqueId());
    }

}
