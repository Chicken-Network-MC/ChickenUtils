package com.chickennw.utils.utils;

import org.bukkit.Location;
import org.bukkit.World;

public class ChunkUtils {

    public static boolean isChunkLoaded(Location location) {
        if (!location.isWorldLoaded()) return false;

        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;

        if (location.getWorld() == null) return false;
        return location.getWorld().isChunkLoaded(chunkX, chunkZ);
    }

    public static boolean isChunkLoaded(World world, int chunkX, int chunkZ) {
        if (world == null) return false;

        return world.isChunkLoaded(chunkX, chunkZ);
    }

}
