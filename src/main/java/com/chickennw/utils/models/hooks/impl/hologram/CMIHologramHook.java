package com.chickennw.utils.models.hooks.impl.hologram;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Modules.Holograms.CMIHologram;
import com.chickennw.utils.models.hooks.AbstractPluginHook;
import com.chickennw.utils.models.hooks.types.HologramHook;
import org.bukkit.Location;

import java.util.List;

@SuppressWarnings("unused")
public class CMIHologramHook extends AbstractPluginHook implements HologramHook {

    public CMIHologramHook() {
        super("CMI Hologram Hook", true, "CMI");
    }

    @Override
    public void load() {

    }

    @Override
    public void unload() {

    }

    @Override
    public boolean isEnabled() {
        return hooksYaml.getBoolean("hologram-hooks.cmi", true);
    }

    @Override
    public void create(Location location, List<String> lines) {
        String locString = locationToString(location);
        CMIHologram oldHologram = CMI.getInstance().getHologramManager().getByName(locString);
        if (oldHologram != null) oldHologram.remove();

        lines = parseColors(lines);

        CMIHologram newHologram = new CMIHologram(locString, location);
        newHologram.setLines(lines);

        CMI.getInstance().getHologramManager().addHologram(newHologram);
        newHologram.update();
    }

    @Override
    public void remove(Location location) {
        String locString = locationToString(location);
        CMIHologram hologram = CMI.getInstance().getHologramManager().getByName(locString);

        if (hologram != null) {
            hologram.remove();
        }
    }

    @Override
    public void update(Location location, List<String> lines) {
        String locString = locationToString(location);
        CMIHologram hologram = CMI.getInstance().getHologramManager().getByName(locString);

        if (hologram != null) {
            lines = parseColors(lines);

            hologram.setLines(lines);
            hologram.update();
        }
    }
}
