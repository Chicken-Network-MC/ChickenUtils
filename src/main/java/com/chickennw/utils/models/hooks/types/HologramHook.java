package com.chickennw.utils.models.hooks.types;

import com.chickennw.utils.models.hooks.PluginHook;
import com.chickennw.utils.utils.ChatUtils;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public interface HologramHook extends PluginHook {

    void create(Location location, List<String> lines);

    void remove(Location location);

    void update(Location location, List<String> lines);

    default String locationToString(Location location) {
        return location.getWorld().getName() + ":" + location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ();
    }

    default List<String> parseColors(List<String> lines) {
        List<String> converted = new ArrayList<>();
        lines.forEach(line -> converted.add(ChatUtils.colorizeLegacy(line)));
        return converted;
    }
}
