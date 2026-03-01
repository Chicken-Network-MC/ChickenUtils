package com.chickennw.utils.models.cross;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class PendingCrossLocationTeleport {

    private final UUID player;
    private final String world;
    private final double x;
    private final double y;
    private final double z;
    private final long timestamp;
}
