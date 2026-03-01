package com.chickennw.utils.models.cross;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class CrossServerPlayer {

    private final UUID uuid;
    private final String name;
    private String server;
    private String world;
}
