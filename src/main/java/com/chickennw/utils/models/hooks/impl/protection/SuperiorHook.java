package com.chickennw.utils.models.hooks.impl.protection;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.events.IslandDisbandEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandKickEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandQuitEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.chickennw.utils.ChickenUtils;
import com.chickennw.utils.events.WrappedIslandDisbandEvent;
import com.chickennw.utils.events.WrappedIslandKickEvent;
import com.chickennw.utils.events.WrappedIslandLeaveEvent;
import com.chickennw.utils.models.hooks.AbstractPluginHook;
import com.chickennw.utils.models.hooks.types.ProtectionHook;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class SuperiorHook extends AbstractPluginHook implements ProtectionHook, Listener {

    public SuperiorHook() {
        super("Superior Skyblock Hook", true, "SuperiorSkyblock2");
    }

    @Override
    public void load() {
        Bukkit.getServer().getPluginManager().registerEvents(this, ChickenUtils.getPlugin());
    }

    @Override
    public void unload() {

    }

    @Override
    public boolean isEnabled() {
        return hooksYaml.getBoolean("protection-hooks.superior-skyblock", true);
    }

    @Override
    public @Nullable UUID getIslandUUID(Location loc) {
        Island island = SuperiorSkyblockAPI.getIslandAt(loc);
        return island == null ? null : island.getUniqueId();
    }

    @Override
    public @Nullable UUID getIslandUUID(OfflinePlayer offlinePlayer) {
        SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayer(offlinePlayer.getUniqueId());
        if (superiorPlayer != null) {
            Island island = superiorPlayer.getIsland();
            return island.getUniqueId();
        }

        return null;
    }

    @Override
    public @Nullable UUID getOwner(Location loc) {
        Island island = SuperiorSkyblockAPI.getIslandAt(loc);
        return island == null ? null : island.getOwner().getUniqueId();
    }

    @Override
    public List<UUID> getMembers(Location loc) {
        Island island = SuperiorSkyblockAPI.getIslandAt(loc);
        if (island != null) {
            return island.getIslandMembers(true)
                    .stream()
                    .map(SuperiorPlayer::getUniqueId)
                    .toList();
        }

        return List.of();
    }

    @Override
    public boolean canPlayerBuild(Location loc, OfflinePlayer offlinePlayer) {
        Island island = SuperiorSkyblockAPI.getIslandAt(loc);
        if (island != null) {
            SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayer(offlinePlayer.getUniqueId());
            return (island.getIslandMembers(true).contains(superiorPlayer) || island.getIslandMembers().contains(superiorPlayer) || island.getOwner()
                    .equals(superiorPlayer) || island.getCoopPlayers().contains(superiorPlayer));
        }

        return false;
    }

    @Override
    public boolean hasProtection(OfflinePlayer offlinePlayer) {
        SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayer(offlinePlayer.getUniqueId());
        return superiorPlayer != null && superiorPlayer.getIsland() != null;
    }

    @Override
    public boolean isInternalChatEnabled(OfflinePlayer offlinePlayer) {
        SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayer(offlinePlayer.getUniqueId());
        return superiorPlayer.hasTeamChatEnabled();
    }

    @Override
    public Location getCenter(Location loc) {
        Island island = SuperiorSkyblockAPI.getIslandAt(loc);
        if (island != null) return island.getCenter(Dimension.getByName(World.Environment.NORMAL.name()));
        return null;
    }

    @Override
    public boolean isInside(Location islandLocation, Location targetLocation) {
        Island island = SuperiorSkyblockAPI.getIslandAt(islandLocation);
        return island.isInside(targetLocation);
    }

    @EventHandler
    public void onIslandDisband(IslandDisbandEvent event) {
        Island island = event.getIsland();
        List<UUID> members = island.getIslandMembers(true)
                .stream()
                .map(SuperiorPlayer::getUniqueId)
                .toList();

        WrappedIslandDisbandEvent wrappedEvent = new WrappedIslandDisbandEvent(island.getUniqueId(), members);
        Bukkit.getServer().getPluginManager().callEvent(wrappedEvent);
    }

    @EventHandler
    public void onIslandKick(IslandKickEvent event) {
        Island island = event.getIsland();
        Player kicker = event.getPlayer().asPlayer();
        UUID player = event.getTarget().getUniqueId();

        WrappedIslandKickEvent wrappedEvent = new WrappedIslandKickEvent(island.getUniqueId(), player, kicker);
        Bukkit.getServer().getPluginManager().callEvent(wrappedEvent);
    }

    @EventHandler
    public void onIslandLeave(IslandQuitEvent event) {
        Island island = event.getIsland();
        UUID player = event.getPlayer().getUniqueId();

        WrappedIslandLeaveEvent wrappedEvent = new WrappedIslandLeaveEvent(island.getUniqueId(), player);
        Bukkit.getServer().getPluginManager().callEvent(wrappedEvent);
    }
}
