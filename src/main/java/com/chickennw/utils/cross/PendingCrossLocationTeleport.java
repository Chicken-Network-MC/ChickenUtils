package com.chickennw.utils.cross;

import java.util.UUID;

public record PendingCrossLocationTeleport(UUID player, String server, String serverGroup, String world, double x, double y, double z,
                                           float yaw, float pitch, long timestamp) {

}
