package com.chickennw.utils.models.hooks.impl.hologram;

import com.chickennw.utils.ChickenUtils;
import com.chickennw.utils.models.hooks.AbstractPluginHook;
import com.chickennw.utils.models.hooks.types.HologramHook;
import de.oliver.fancyholograms.api.FancyHologramsPlugin;
import de.oliver.fancyholograms.api.HologramManager;
import de.oliver.fancyholograms.api.data.TextHologramData;
import de.oliver.fancyholograms.api.hologram.Hologram;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Display.Billboard;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
public class FancyHologramsHook extends AbstractPluginHook implements HologramHook {

    private HologramManager manager;
    private boolean textShadow;
    private Color backgroundColor;
    private Billboard billboard;

    public FancyHologramsHook() {
        super("Fancy Holograms Hook", true, "FancyHolograms");
    }

    @Override
    public void load() {
        manager = FancyHologramsPlugin.get().getHologramManager();
        textShadow = true;
        billboard = Billboard.VERTICAL;
        backgroundColor = Color.fromARGB(0, 0, 0, 0);
    }

    @Override
    public void unload() {

    }

    @Override
    public boolean isEnabled() {
        return hooksYaml.getBoolean("hologram-hooks.fancy-holograms", true);
    }

    @Override
    public void create(Location location, List<String> lines) {
        String locString = locationToString(location);
        Optional<Hologram> oldHologram = manager.getHologram(locString);
        oldHologram.ifPresent(Hologram::deleteHologram);

        Location holoLoc = location.clone();
        holoLoc.add(0, -1, 0);
        holoLoc.setYaw(0);
        holoLoc.setPitch(0);

        FileConfiguration config = ChickenUtils.getPlugin().getConfig();
        int range = config.isSet("hologram-visibility-range") ? config.getInt("hologram-visibility-range") : 30;

        TextHologramData hologramData = new TextHologramData(locString, holoLoc);
        hologramData.setBillboard(billboard);
        hologramData.setPersistent(false);
        hologramData.setTextShadow(textShadow);
        hologramData.setBackground(backgroundColor);
        hologramData.setTranslation(new Vector3f(0, 0, 0));
        hologramData.setVisibilityDistance(range);

        lines = parseColors(lines);
        hologramData.setText(lines);

        Hologram hologram = manager.create(hologramData);
        manager.addHologram(hologram);
    }

    @Override
    public void remove(Location location) {
        Location holoLoc = location.clone();
        holoLoc.add(0, -1, 0);

        manager.getHologram(locationToString(holoLoc)).ifPresent(manager::removeHologram);
    }

    @Override
    public void update(Location location, List<String> lines) {
        Location holoLoc = location.clone();
        holoLoc.add(0, -1, 0);

        String locString = locationToString(holoLoc);
        Hologram hologram = manager.getHologram(locString).orElse(null);

        if (hologram == null) {
            create(holoLoc, lines);
        } else {
            TextHologramData hologramData = (TextHologramData) hologram.getData();
            if (hologramData.getText().size() == lines.size()) {
                lines = parseColors(lines);

                hologramData.setText(lines);
                hologram.queueUpdate();
            } else {
                remove(holoLoc);
                create(holoLoc, lines);
            }
        }
    }
}
