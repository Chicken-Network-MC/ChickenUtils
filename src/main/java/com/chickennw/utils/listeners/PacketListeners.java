package com.chickennw.utils.listeners;

import com.chickennw.utils.ChickenUtils;
import com.chickennw.utils.managers.NPCManager;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.tcoded.folialib.FoliaLib;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PacketListeners implements PacketListener {

    private final List<UUID> recentlyClicked = new ArrayList<>();
    private final PacketListenerCommon packetListenerCommon;

    public PacketListeners() {
        packetListenerCommon = PacketEvents.getAPI().getEventManager().registerListener(this, PacketListenerPriority.NORMAL);
    }

    public void disable() {
        PacketEvents.getAPI().getEventManager().unregisterListeners(packetListenerCommon);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.INTERACT_ENTITY) return;

        NPCManager npcManager = NPCManager.getInstance();
        WrapperPlayClientInteractEntity eventWrapper = new WrapperPlayClientInteractEntity(event);
        Player player = event.getPlayer();
        int entityId = eventWrapper.getEntityId();

        if (player == null) return;
        if (recentlyClicked.contains(player.getUniqueId())) return;
        recentlyClicked.add(player.getUniqueId());

        FoliaLib foliaLib = ChickenUtils.getFoliaLib();
        foliaLib.getScheduler().runLater(() -> recentlyClicked.remove(player.getUniqueId()), 5);

        npcManager.getNpcs().values()
                .stream()
                .filter(Objects::nonNull)
                .filter(npc -> npc.getEntityId() == entityId)
                .forEach(npc -> npc.execute(player));
    }

}
