package com.chickennw.utils.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
public class WrappedIslandKickEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final UUID island;
    private final UUID player;
    private final Player kicker;
    private boolean cancelled = false;

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}

