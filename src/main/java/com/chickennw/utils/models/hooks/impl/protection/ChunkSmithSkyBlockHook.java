package com.chickennw.utils.models.hooks.impl.protection;

import com.chickennw.utils.ChickenUtils;
import com.chickennw.utils.events.WrappedIslandDisbandEvent;
import com.chickennw.utils.events.WrappedIslandKickEvent;
import com.chickennw.utils.events.WrappedIslandLeaveEvent;
import com.chickennw.utils.models.hooks.AbstractPluginHook;
import com.chickennw.utils.models.hooks.types.ProtectionHook;
import net.chunksmith.skyblock.ChunkSmithSkyBlock;
import net.chunksmith.skyblock.events.IslandDisbandEvent;
import net.chunksmith.skyblock.events.IslandMemberKickEvent;
import net.chunksmith.skyblock.events.IslandMemberLeaveEvent;
import net.chunksmith.skyblock.managers.island.IIslandManager;
import net.chunksmith.skyblock.managers.player.IPlayerManager;
import net.chunksmith.skyblock.managers.world.IIslandWorldManager;
import net.chunksmith.skyblock.models.Island;
import net.chunksmith.skyblock.models.SkyPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ChunkSmithSkyBlockHook extends AbstractPluginHook implements ProtectionHook, Listener {

    private IIslandManager islandManager;
    private IIslandWorldManager worldManager;
    private IPlayerManager playerManager;

    public ChunkSmithSkyBlockHook() {
        super("ChunkSmithSkyBlock Hook", true, "ChunkSmithSkyBlock");
    }

    @Override
    public void load() {
        ChunkSmithSkyBlock plugin = ChunkSmithSkyBlock.getPlugin(ChunkSmithSkyBlock.class);
        islandManager = plugin.getInjector().getInstance(IIslandManager.class);
        worldManager = plugin.getInjector().getInstance(IIslandWorldManager.class);
        playerManager = plugin.getInjector().getInstance(IPlayerManager.class);
        Bukkit.getServer().getPluginManager().registerEvents(this, ChickenUtils.getPlugin());
    }

    @Override
    public void unload() {

    }

    @Override
    public boolean isEnabled() {
        return hooksYaml.getBoolean("protection-hooks.chunksmith-skyblock", true);
    }

    @Override
    public @Nullable UUID getIslandUUID(Location loc) {
        Island island = getIsland(loc);
        return island == null ? null : island.getUuid();
    }

    @Override
    public @Nullable UUID getIslandUUID(OfflinePlayer offlinePlayer) {
        return islandManager.getByPlayer(offlinePlayer.getUniqueId()).map(Island::getUuid).orElse(null);
    }

    @Override
    public @Nullable UUID getOwner(Location loc) {
        Island island = getIsland(loc);
        return island == null ? null : island.getOwner();
    }

    @Override
    public List<UUID> getMembers(Location loc) {
        Island island = getIsland(loc);
        return island == null ? List.of() : new ArrayList<>(island.getMembers().keySet());
    }

    @Override
    public boolean canPlayerBuild(Location loc, OfflinePlayer offlinePlayer) {
        Island island = getIsland(loc);
        return island != null && island.isTrusted(offlinePlayer.getUniqueId());
    }

    @Override
    public boolean hasProtection(OfflinePlayer offlinePlayer) {
        return islandManager.getByPlayer(offlinePlayer.getUniqueId()).isPresent();
    }

    @Override
    public boolean isInternalChatEnabled(OfflinePlayer offlinePlayer) {
        return playerManager.get(offlinePlayer.getUniqueId()).map(SkyPlayer::isTeamChat).orElse(false);
    }

    @Override
    public Location getCenter(Location loc) {
        Island island = getIsland(loc);
        return island == null ? null : island.homeSpawn().toLocation(loc.getWorld());
    }

    @Override
    public boolean isInside(Location islandLocation, Location targetLocation) {
        Island island = getIsland(islandLocation);
        return island != null && Objects.equals(island, getIsland(targetLocation));
    }

    @EventHandler
    public void onIslandDisband(IslandDisbandEvent event) {
        Island island = event.getIsland();
        List<UUID> members = new ArrayList<>(island.getMembers().keySet());
        Bukkit.getPluginManager().callEvent(new WrappedIslandDisbandEvent(island.getUuid(), members));
    }

    @EventHandler
    public void onMemberKick(IslandMemberKickEvent event) {
        UUID island = event.getIsland().getUuid();
        Bukkit.getPluginManager().callEvent(new WrappedIslandKickEvent(island, event.getMember().player(), event.getPlayer()));
    }

    @EventHandler
    public void onMemberLeave(IslandMemberLeaveEvent event) {
        UUID island = event.getIsland().getUuid();
        Bukkit.getPluginManager().callEvent(new WrappedIslandLeaveEvent(island, event.getMember().player()));
    }

    private @Nullable Island getIsland(Location loc) {
        World world = loc == null ? null : loc.getWorld();
        return world == null ? null : worldManager.islandOf(world).orElse(null);
    }
}
