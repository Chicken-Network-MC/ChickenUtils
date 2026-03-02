package com.chickennw.utils.utils;

import com.chickennw.utils.ChickenUtils;
import com.chickennw.utils.models.cross.PendingCrossLocationTeleport;
import com.chickennw.utils.models.cross.PendingCrossPlayerTeleport;
import com.google.common.cache.Cache;

import java.util.UUID;

public class CrossTeleportUtils {

    public static void teleportLocation(PendingCrossLocationTeleport location) {
        Cache<UUID, PendingCrossLocationTeleport> locationTeleportRequests = ChickenUtils.getLocationTeleportRequests();
        locationTeleportRequests.put(location.getPlayer(), location);
    }

    public static void teleportToPlayer(PendingCrossPlayerTeleport player) {
        Cache<UUID, PendingCrossPlayerTeleport> locationTeleportRequests = ChickenUtils.getPlayerTeleportRequests();
        locationTeleportRequests.put(player.getPlayer(), player);
    }
}
