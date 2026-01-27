package com.chickennw.utils.models.hooks.impl.protection;

import com.chickennw.utils.ChickenUtils;
import com.chickennw.utils.events.WrappedIslandDisbandEvent;
import com.chickennw.utils.models.hooks.AbstractPluginHook;
import com.chickennw.utils.models.hooks.types.ProtectionHook;
import dev.espi.protectionstones.PSPlayer;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.event.PSRemoveEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProtectionStonesHook extends AbstractPluginHook implements ProtectionHook, Listener {

    public ProtectionStonesHook() {
        super("Superior Skyblock Hook", true, "ProtectionStones");
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
        return hooksYaml.getBoolean("protection-hooks.protection-hooks", true);
    }

    @EventHandler
    public void onRegionRemove(PSRemoveEvent event) {
        PSRegion region = event.getRegion();
        List<UUID> members = region.getMembers();

        WrappedIslandDisbandEvent wrappedEvent = new WrappedIslandDisbandEvent(null, members);
        Bukkit.getServer().getPluginManager().callEvent(wrappedEvent);
    }

    @Override
    public boolean canPlayerBuild(Location loc, OfflinePlayer offlinePlayer) {
        PSRegion region = PSRegion.fromLocation(loc);
        if (region != null) {
            return (region.isMember(offlinePlayer.getUniqueId()) || region.isOwner(offlinePlayer.getUniqueId()));
        }

        return false;
    }

    @Override
    public boolean hasProtection(OfflinePlayer offlinePlayer) {
        PSPlayer psPlayer = PSPlayer.fromPlayer(offlinePlayer);
        return !psPlayer.getPSRegions(offlinePlayer.getPlayer().getWorld(), true).isEmpty();
    }

    @Override
    public boolean isInternalChatEnabled(OfflinePlayer offlinePlayer) {
        return false;
    }

    @Override
    public Location getCenter(Location loc) {
        PSRegion region = PSRegion.fromLocation(loc);
        if (region != null) {
            return region.getProtectBlock().getLocation();
        }

        return null;
    }

    @Override
    public boolean isInside(Location islandLocation, Location targetLocation) {
        PSRegion region = PSRegion.fromLocation(islandLocation);
        PSRegion targetRegion = PSRegion.fromLocation(targetLocation);
        return region != null && region.equals(targetRegion);
    }

    @Override
    public ArrayList<UUID> getMembers(Location loc) {
        PSRegion region = PSRegion.fromLocation(loc);
        return region != null ? new ArrayList<>(region.getMembers()) : new ArrayList<>();
    }

    @Override
    public @Nullable UUID getIslandUUID(Location loc) {
        PSRegion region = PSRegion.fromLocation(loc);
        return region != null ? UUID.fromString(region.getId()) : null;
    }

    @Override
    public @Nullable UUID getIslandUUID(OfflinePlayer offlinePlayer) {
        PSPlayer psPlayer = PSPlayer.fromPlayer(offlinePlayer);
        List<PSRegion> regions = psPlayer.getPSRegions(offlinePlayer.getPlayer().getWorld(), true);
        if (!regions.isEmpty()) return UUID.fromString(regions.getFirst().getId());

        return null;
    }

    @Override
    public UUID getOwner(Location loc) {
        PSRegion region = PSRegion.fromLocationGroup(loc);
        if (region != null) {
            return region.getLandlord();
        }

        return null;
    }
}
