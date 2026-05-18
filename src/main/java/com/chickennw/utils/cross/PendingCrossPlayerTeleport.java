package com.chickennw.utils.cross;

import java.util.UUID;

public record PendingCrossPlayerTeleport(UUID player, UUID target, String server, long timestamp) {

}
