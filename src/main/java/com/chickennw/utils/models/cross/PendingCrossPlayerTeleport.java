package com.chickennw.utils.models.cross;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class PendingCrossPlayerTeleport {

    private final UUID player;
    private final UUID target;
    private final long timestamp;
}
