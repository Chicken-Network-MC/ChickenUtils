package com.chickennw.utils.models.hooks.types;

import com.chickennw.utils.models.hooks.PluginHook;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface ProtectionHook extends PluginHook {

    @Nullable
    UUID getIslandUUID(Location loc);

    @Nullable
    UUID getIslandUUID(Player player);

    @Nullable
    UUID getOwner(Location loc);

    List<UUID> getMembers(Location loc);

    boolean canPlayerBuild(Location loc, Player player);

    boolean hasProtection(Player player);

    boolean isInternalChatEnabled(Player player);

    Location getCenter(Location loc);

    boolean isInside(Location islandLocation, Location targetLocation);
}
