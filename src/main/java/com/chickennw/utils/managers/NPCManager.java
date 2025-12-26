package com.chickennw.utils.managers;

import com.chickennw.utils.ChickenUtils;
import com.chickennw.utils.logger.LoggerFactory;
import com.chickennw.utils.models.npc.NPC;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Getter
@Setter
@SuppressWarnings("unused")
public class NPCManager {

    private static NPCManager instance;
    private final ConcurrentHashMap<UUID, NPC> npcs = new ConcurrentHashMap<>();
    private final Plugin plugin;
    private final Logger logger;
    private final ExecutorService executor;

    public static NPCManager getInstance() {
        if (instance == null) instance = new NPCManager();

        return instance;
    }

    private NPCManager() {
        plugin = ChickenUtils.getPlugin();
        logger = LoggerFactory.getLogger();

        ThreadFactory factory = Thread.ofVirtual()
                .name(plugin.getName() + "-npc-worker-", 0)
                .uncaughtExceptionHandler((thread, throwable) -> logger.error(throwable.getMessage(), throwable))
                .factory();
        executor = Executors.newSingleThreadExecutor(factory);
    }

    public void registerNPC(NPC npc) {
        npcs.put(npc.getUuid(), npc);
    }

    public void deleteNPC(UUID uuid) {
        NPC npc = npcs.get(uuid);
        npcs.remove(uuid);

        if (npc == null) return;
        npc.despawn();
    }
}
