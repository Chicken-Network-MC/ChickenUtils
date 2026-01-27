package com.chickennw.utils.models.hooks.impl.protection;

import com.chickennw.utils.ChickenUtils;
import com.chickennw.utils.events.WrappedIslandDisbandEvent;
import com.chickennw.utils.events.WrappedIslandLeaveEvent;
import com.chickennw.utils.models.hooks.AbstractPluginHook;
import com.chickennw.utils.models.hooks.types.ProtectionHook;
import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.events.LandDeleteEvent;
import me.angeschossen.lands.api.events.PlayerLeaveLandEvent;
import me.angeschossen.lands.api.land.Land;
import me.angeschossen.lands.api.player.LandPlayer;
import me.angeschossen.lands.api.player.chat.ChatMode;
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

public class LandsHook extends AbstractPluginHook implements ProtectionHook, Listener {

    private LandsIntegration landsIntegration;

    public LandsHook() {
        super("Superior Skyblock Hook", true, "Lands");
    }

    @Override
    public void load() {
        landsIntegration = LandsIntegration.of(ChickenUtils.getPlugin());
        Bukkit.getServer().getPluginManager().registerEvents(this, ChickenUtils.getPlugin());
    }

    @Override
    public void unload() {

    }

    @Override
    public boolean isEnabled() {
        return hooksYaml.getBoolean("protection-hooks.lands", true);
    }

    @Override
    public @Nullable UUID getIslandUUID(Location loc) {
        Land land = getLandAt(loc);
        return land != null ? land.getOwnerUID() : null; // Temp solution
    }

    @Override
    public @Nullable UUID getIslandUUID(OfflinePlayer offlinePlayer) {
        LandPlayer landPlayer = landsIntegration.getLandPlayer(offlinePlayer.getUniqueId());
        return landPlayer.getLands().isEmpty() ? null : landPlayer.getLands().iterator().next().getOwnerUID();
    }

    @Override
    public @Nullable UUID getOwner(Location loc) {
        Land land = getLandAt(loc);
        return land != null ? land.getOwnerUID() : null;
    }

    @Override
    public List<UUID> getMembers(Location loc) {
        List<UUID> members = new ArrayList<>();
        Land land = getLandAt(loc);

        if (land != null) members.addAll(land.getTrustedPlayers());
        return members;
    }

    @Override
    public boolean canPlayerBuild(Location loc, OfflinePlayer offlinePlayer) {
        List<UUID> members = getMembers(loc);
        UUID owner = getOwner(loc);

        if (owner != null) return members.contains(offlinePlayer.getUniqueId()) || owner.equals(offlinePlayer.getUniqueId());
        return false;
    }

    @Override
    public boolean hasProtection(OfflinePlayer offlinePlayer) {
        LandPlayer landPlayer = landsIntegration.getLandPlayer(offlinePlayer.getUniqueId());
        return !landPlayer.getLands().isEmpty();
    }

    @Override
    public boolean isInternalChatEnabled(OfflinePlayer offlinePlayer) {
        ChatMode mode = landsIntegration.getLandPlayer(offlinePlayer.getUniqueId()).getChatMode();
        return mode == ChatMode.LAND;
    }

    @Override
    public Location getCenter(Location loc) {
        Land land = getLandAt(loc);
        return land != null && land.getSpawnPosition() != null ? land.getSpawnPosition().toLocation() : null;
    }

    @Override
    public boolean isInside(Location islandLocation, Location targetLocation) {
        Land land = getLandAt(islandLocation);
        return land != null && land.getArea(targetLocation) != null;
    }

    @EventHandler
    public void onLandDelete(LandDeleteEvent e) {
        Land land = e.getLand();
        List<UUID> members = new ArrayList<>(land.getTrustedPlayers());
        members.add(land.getOwnerUID());

        WrappedIslandDisbandEvent wrappedEvent = new WrappedIslandDisbandEvent(land.getOwnerUID(), members);
        Bukkit.getServer().getPluginManager().callEvent(wrappedEvent);
    }

    @EventHandler
    public void onLandLeave(PlayerLeaveLandEvent e) {
        Land land = e.getLand();
        LandPlayer player = e.getLandPlayer();

        WrappedIslandLeaveEvent wrappedEvent = new WrappedIslandLeaveEvent(land.getOwnerUID(), player.getUID());
        Bukkit.getServer().getPluginManager().callEvent(wrappedEvent);
    }

    private Land getLandAt(Location loc) {
        World world = loc.getWorld();
        if (world == null) return null;

        int chunkX = loc.getChunk().getX();
        int chunkZ = loc.getChunk().getZ();

        return landsIntegration.getLandByUnloadedChunk(world, chunkX, chunkZ);
    }
}
