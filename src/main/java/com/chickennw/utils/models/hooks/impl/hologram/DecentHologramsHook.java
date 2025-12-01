package com.chickennw.utils.models.hooks.impl.hologram;

import com.chickennw.utils.ChickenUtils;
import com.chickennw.utils.models.hooks.AbstractPluginHook;
import com.chickennw.utils.models.hooks.types.HologramHook;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.holograms.HologramPage;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

@SuppressWarnings("unused")
public class DecentHologramsHook extends AbstractPluginHook implements HologramHook {

    public DecentHologramsHook() {
        super("Decent Holograms Hook", true, "DecentHolograms");
    }

    @Override
    public void load() {

    }

    @Override
    public void unload() {

    }

    @Override
    public boolean isEnabled() {
        return hooksYaml.getBoolean("hologram-hooks.decent-holograms", true);
    }

    @Override
    public void create(Location location, List<String> lines) {
        String locString = locationToString(location);
        Hologram oldHologram = DHAPI.getHologram(locString);
        if (oldHologram != null) {
            oldHologram.delete();
        }

        lines = parseColors(lines);
        Hologram hologram = DHAPI.createHologram(locString, location, lines);

        FileConfiguration config = ChickenUtils.getPlugin().getConfig();
        int range = config.isSet("hologram-visibility-range") ? config.getInt("hologram-visibility-range") : 30;
        hologram.setDisplayRange(range);
        hologram.setUpdateRange(range);
    }

    @Override
    public void remove(Location location) {
        DHAPI.removeHologram(locationToString(location));
    }

    @Override
    public void update(Location location, List<String> lines) {
        String locString = locationToString(location);
        Hologram hologram = DHAPI.getHologram(locString);

        if (hologram == null) {
            create(location, lines);
        } else {
            HologramPage page = hologram.getPage(0);
            lines = parseColors(lines);

            if (lines.size() == page.getLines().size()) {
                int i = 0;
                for (String lineText : lines) {
                    page.setLine(i, lineText);
                    i++;
                }
            } else {
                remove(location);
                create(location, lines);
            }
        }
    }
}
