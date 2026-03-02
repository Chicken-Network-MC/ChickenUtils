package com.chickennw.utils.listeners.cross;

import com.chickennw.utils.ChickenUtils;
import com.chickennw.utils.models.cross.PendingCrossLocationTeleport;
import com.chickennw.utils.models.cross.PendingCrossPlayerTeleport;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class CrossServerTeleportListeners implements Listener {

    @EventHandler
    public void onPlayerJoinPlayerTeleport(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        PendingCrossPlayerTeleport request = ChickenUtils.getPlayerTeleportRequests().getIfPresent(player.getUniqueId());
        if (request != null) {
            UUID target = request.getTarget();
            Player targetPlayer = Bukkit.getPlayer(target);
            if (targetPlayer == null) return;

            teleport(player, targetPlayer.getLocation());
        }
    }

    @EventHandler
    public void onPlayerJoinLocationTeleport(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        PendingCrossLocationTeleport request = ChickenUtils.getLocationTeleportRequests().getIfPresent(player.getUniqueId());
        if (request != null) {
            String world = request.getWorld();
            double x = request.getX();
            double y = request.getY();
            double z = request.getZ();

            Location targetLocation = new Location(Bukkit.getWorld(world), x, y, z);
            teleport(player, targetLocation);
        }
    }

    private void teleport(Player player, Location location) {
        ChickenUtils.getFoliaLib().getScheduler().runAtEntityLater(player, (task) -> {
            player.teleportAsync(location);
            ChickenUtils.getLocationTeleportRequests().invalidate(player.getUniqueId());
            ChickenUtils.getPlayerTeleportRequests().invalidate(player.getUniqueId());
        }, 5);
    }
}
