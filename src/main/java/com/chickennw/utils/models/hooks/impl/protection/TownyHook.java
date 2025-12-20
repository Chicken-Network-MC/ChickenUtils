package com.chickennw.utils.models.hooks.impl.protection;

import com.chickennw.utils.ChickenUtils;
import com.chickennw.utils.events.WrappedIslandDisbandEvent;
import com.chickennw.utils.events.WrappedIslandKickEvent;
import com.chickennw.utils.models.hooks.AbstractPluginHook;
import com.chickennw.utils.models.hooks.types.ProtectionHook;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.town.TownKickEvent;
import com.palmergames.bukkit.towny.event.town.TownRuinedEvent;
import com.palmergames.bukkit.towny.object.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TownyHook extends AbstractPluginHook implements ProtectionHook, Listener {

    public TownyHook() {
        super("Towny Hook", true, "Towny");
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
        return hooksYaml.getBoolean("protection-hooks.towny", true);
    }

    @Override
    public @Nullable UUID getIslandUUID(Location loc) {
        Town town = TownyAPI.getInstance().getTown(loc);
        return town == null ? null : town.getUUID();
    }

    @Override
    public @Nullable UUID getIslandUUID(Player player) {
        Town town = TownyAPI.getInstance().getTown(player);
        return town == null ? null : town.getUUID();
    }

    @Override
    public @Nullable UUID getOwner(Location loc) {
        Town town = TownyAPI.getInstance().getTown(loc);
        if (town == null) return null;
        if (!town.hasMayor()) return null;
        return town.getMayor().getUUID();
    }

    @Override
    public List<UUID> getMembers(Location loc) {
        Town town = TownyAPI.getInstance().getTown(loc);
        if (town == null) return new ArrayList<>();

        return town.getResidents().stream().map(Resident::getUUID).toList();
    }

    @Override
    public boolean canPlayerBuild(Location loc, Player player) {
        Town town = TownyAPI.getInstance().getTown(loc);
        if (town == null) return false;

        Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident == null) return false;

        return town.hasResident(resident);
    }

    @Override
    public boolean hasProtection(Player player) {
        Town town = TownyAPI.getInstance().getTown(player);
        return town != null;
    }

    @Override
    public boolean isInternalChatEnabled(Player player) {
        return false;
    }

    @Override
    public Location getCenter(Location loc) {
        Town town = TownyAPI.getInstance().getTown(loc);
        if (town == null) return null;

        TownBlock townBlock = town.getHomeBlockOrNull();
        if (townBlock == null) return null;

        WorldCoord coord = townBlock.getWorldCoord();
        return coord.getUpperMostCornerLocation();
    }

    @Override
    public boolean isInside(Location loc) {
        Town town = TownyAPI.getInstance().getTown(loc);
        return town != null;
    }

    @EventHandler
    public void onTownDisband(TownRuinedEvent event) {
        Town town = event.getTown();
        List<UUID> members = town.getResidents().stream().map(Resident::getUUID).toList();

        WrappedIslandDisbandEvent wrappedEvent = new WrappedIslandDisbandEvent(town.getUUID(), members);
        Bukkit.getServer().getPluginManager().callEvent(wrappedEvent);
    }

    @EventHandler
    public void onTownKick(TownKickEvent event) {
        Town town = event.getTown();
        Resident kickedResident = event.getKickedResident();

        WrappedIslandKickEvent wrappedEvent = new WrappedIslandKickEvent(town.getUUID(), kickedResident.getUUID(), null);
        Bukkit.getServer().getPluginManager().callEvent(wrappedEvent);
    }
}
