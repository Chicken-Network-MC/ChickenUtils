package com.chickennw.utils.models.npc;

import com.chickennw.utils.ChickenUtils;
import com.chickennw.utils.managers.NPCManager;
import com.chickennw.utils.utils.ChunkUtils;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Getter
@Setter
public abstract class NPC {

    protected final UUID uuid = UUID.randomUUID();
    protected final int entityId = SpigotReflectionUtil.generateEntityId();
    protected ConcurrentHashMap<UUID, UUID> seeingPlayers; // player - player

    protected boolean visible = true;
    protected List<Equipment> equipments;
    protected EntityType entityType;
    protected String name;
    protected String worldName;
    protected double x;
    protected double y;
    protected double z;
    protected float yaw;
    protected float pitch;
    protected Consumer<Player> onClickAction;
    protected boolean despawned;

    protected WrappedTask updateTask;
    protected Location location;

    public NPC(String name, String worldName, double x, double y, double z, EntityType entityType, Consumer<Player> onClickAction) {
        this.name = name;
        this.onClickAction = onClickAction;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        yaw = 0;
        pitch = 0;
        equipments = new ArrayList<>();
        this.entityType = entityType;

        despawned = !isChunkLoaded();
    }

    public void startUpdateTask() {
        if (updateTask != null) updateTask.cancel();

        FoliaLib foliaLib = ChickenUtils.getFoliaLib();
        updateTask = foliaLib.getScheduler().runTimerAsync(() -> {
            if (despawned) return;
            if (location == null) getLocation();

            if (!location.isWorldLoaded()) return;
            if (!isChunkLoaded()) return;

            World world = location.getWorld();
            if (world == null) return;

            seeingPlayers.values().forEach(playerUUID -> {
                Player player = Bukkit.getPlayer(playerUUID);
                if (player == null) {
                    seeingPlayers.remove(playerUUID);
                    return;
                }

                Location playerLocation = player.getLocation();
                if (!Objects.equals(playerLocation.getWorld(), world)) despawn(player, true);
            });

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().startsWith("Loader-")) continue; // Wild Loaders
                if (!Objects.equals(player.getLocation().getWorld(), location.getWorld())) continue;
                if (player.getLocation().distance(location) > 31) {
                    if (seeingPlayers.contains(player.getUniqueId())) despawn(player, false);
                    continue;
                }

                if (!seeingPlayers.contains(player.getUniqueId())) spawn(player);
                seeingPlayers.put(player.getUniqueId(), player.getUniqueId());
            }
        }, 0, 10);
    }

    public void execute(Player player) {
        onClickAction.accept(player);
    }

    public void spawn(Player player) {
        if (!visible) return;
        despawned = false;

        Location location = getLocation();
        com.github.retrooper.packetevents.protocol.world.Location spawnLocation = SpigotConversionUtil.fromBukkitLocation(location);

        WrapperPlayServerSpawnEntity spawnPacket = new WrapperPlayServerSpawnEntity(entityId, uuid, entityType, spawnLocation, location.getYaw(), 0, null);

        queuePacket(spawnPacket, player);

        FoliaLib foliaLib = ChickenUtils.getFoliaLib();
        foliaLib.getScheduler().runLater(this::update, 1);
    }

    public void update() {
        if (!visible) return;
        if (despawned) return;

        updateRotation();
        updateEquipment(equipments);
    }

    public void updateEquipment(List<Equipment> equipments) {
        if (despawned) return;

        this.equipments = equipments;
        if (equipments == null || equipments.isEmpty()) return;

        WrapperPlayServerEntityEquipment equipmentPacket = new WrapperPlayServerEntityEquipment(entityId, equipments);
        queuePacketSeeing(equipmentPacket);
    }

    public void updateRotation() {
        if (despawned) return;

        WrapperPlayServerEntityRotation packet = new WrapperPlayServerEntityRotation(entityId, yaw, pitch, false);
        queuePacketSeeing(packet);
    }

    public void despawn() {
        despawned = true;

        seeingPlayers.forEach((uuid, playerUUID) -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) despawn(player, true);
        });

        seeingPlayers.clear();
    }

    public void despawn(Player player, boolean sync) {
        seeingPlayers.remove(player.getUniqueId());

        WrapperPlayServerDestroyEntities packet = new WrapperPlayServerDestroyEntities(entityId);

        if (sync) sendPacketSync(packet, player);
        else queuePacket(packet, player);
    }

    public void setGlowing(boolean glowing) {
        if (!visible) return;
        if (despawned) return;

        List<EntityData<?>> entityDataList = new ArrayList<>();
        EntityData<?> data2 = new EntityData<>(4, EntityDataTypes.BOOLEAN, glowing);
        entityDataList.add(data2);

        WrapperPlayServerEntityMetadata modifyPacket = new WrapperPlayServerEntityMetadata(entityId, entityDataList);
        queuePacketSeeing(modifyPacket);
    }

    public void queuePacketSeeing(PacketWrapper<?> packet) {
        Runnable runnable = () -> Bukkit.getOnlinePlayers().forEach(player -> {
            UUID playerUUID = player.getUniqueId();
            if (seeingPlayers.contains(playerUUID)) queuePacket(packet, player);
        });

        NPCManager.getInstance().getExecutor().submit(runnable);
    }

    public void queuePacket(PacketWrapper<?> packet) {
        Runnable runnable = () -> Bukkit.getOnlinePlayers().forEach(player -> sendPacket(packet, player));

        NPCManager.getInstance().getExecutor().submit(runnable);
    }

    public void queuePacket(PacketWrapper<?> packet, Player player) {
        Runnable runnable = () -> sendPacket(packet, player);

        NPCManager.getInstance().getExecutor().submit(runnable);
    }

    private void sendPacket(PacketWrapper<?> packet, Player player) {
        FoliaLib foliaLib = ChickenUtils.getFoliaLib();
        foliaLib.getScheduler().runAsync((task) -> sendPacketSync(packet, player));
    }

    private void sendPacketSync(PacketWrapper<?> packet, Player player) {
        try {
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
        } catch (Exception ex) {
            Bukkit.getConsoleSender()
                    .sendMessage("§b[LitLibs] §cFailed to send packet: " + packet.toString() + " on player: " + player.getName() + " with uuid: " + player.getUniqueId());
        }
    }

    public boolean isChunkLoaded() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return false;

        return ChunkUtils.isChunkLoaded(getLocation());
    }

    public int getChunkX() {
        return (int) x >> 4;
    }

    public int getChunkZ() {
        return (int) z >> 4;
    }

    public Location getLocation() {
        if (location == null) location = new Location(Bukkit.getWorld(worldName), x, y, z);
        return location;
    }

    public void setLocation(Location location) {
        x = location.getX();
        y = location.getY();
        z = location.getZ();
        worldName = location.getWorld().getName();
        this.location = location;
    }
}
