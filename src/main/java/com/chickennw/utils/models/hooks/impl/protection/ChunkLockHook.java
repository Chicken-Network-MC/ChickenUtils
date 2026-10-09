package com.chickennw.utils.models.hooks.impl.protection;

import com.chickennw.utils.ChickenUtils;
import com.chickennw.utils.events.WrappedIslandDisbandEvent;
import com.chickennw.utils.events.WrappedIslandKickEvent;
import com.chickennw.utils.models.hooks.AbstractPluginHook;
import com.chickennw.utils.models.hooks.types.ProtectionHook;
import me.waterarchery.chunklock.ChunkLock;
import me.waterarchery.chunklock.api.events.BoxDisbandEvent;
import me.waterarchery.chunklock.api.events.BoxMemberRemoveEvent;
import me.waterarchery.chunklock.models.Box;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChunkLockHook extends AbstractPluginHook implements ProtectionHook, Listener {

    public ChunkLockHook() {
        super("ChunkLock Hook", true, "ChunkLock");
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
        return hooksYaml.getBoolean("protection-hooks.chunk-lock", true);
    }

    @Override
    public @Nullable UUID getIslandUUID(Location loc) {
        Box box = getBox(loc);
        return box == null ? null : box.getUuid();
    }

    @Override
    public @Nullable UUID getIslandUUID(OfflinePlayer offlinePlayer) {
        Box box = ChunkLock.getBoxManager().getBoxByPlayer(offlinePlayer.getUniqueId());
        return box == null ? null : box.getUuid();
    }

    @Override
    public @Nullable UUID getOwner(Location loc) {
        Box box = getBox(loc);
        return box == null ? null : box.getOwner();
    }

    @Override
    public List<UUID> getMembers(Location loc) {
        Box box = getBox(loc);
        return box == null ? List.of() : new ArrayList<>(box.getMembers().keySet());
    }

    @Override
    public boolean canPlayerBuild(Location loc, OfflinePlayer offlinePlayer) {
        Box box = getBox(loc);
        return box != null && box.canBuild(offlinePlayer.getUniqueId());
    }

    @Override
    public boolean hasProtection(OfflinePlayer offlinePlayer) {
        return ChunkLock.getBoxManager().getBoxByPlayer(offlinePlayer.getUniqueId()) != null;
    }

    @Override
    public boolean isInternalChatEnabled(OfflinePlayer offlinePlayer) {
        return false;
    }

    @Override
    public Location getCenter(Location loc) {
        Box box = getBox(loc);
        World world = loc.getWorld();
        if (box == null || world == null) return null;
        return box.getSpawn() != null ? box.getSpawn().toLocation(world) : world.getSpawnLocation();
    }

    @Override
    public boolean isInside(Location islandLocation, Location targetLocation) {
        Box box = getBox(islandLocation);
        return box != null && box.equals(getBox(targetLocation));
    }

    @EventHandler
    public void onBoxDisband(BoxDisbandEvent event) {
        Box box = event.getBox();
        List<UUID> members = new ArrayList<>(box.getMembers().keySet());
        Bukkit.getPluginManager().callEvent(new WrappedIslandDisbandEvent(box.getUuid(), members));
    }

    @EventHandler
    public void onMemberRemove(BoxMemberRemoveEvent event) {
        Bukkit.getPluginManager().callEvent(new WrappedIslandKickEvent(event.getBox().getUuid(), event.getMember(), null));
    }

    private @Nullable Box getBox(Location loc) {
        World world = loc == null ? null : loc.getWorld();
        return world == null ? null : ChunkLock.getBoxManager().getBoxByWorld(world.getName());
    }
}
