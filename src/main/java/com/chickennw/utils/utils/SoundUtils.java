package com.chickennw.utils.utils;

import com.chickennw.utils.ChickenUtils;
import com.cryptomorin.xseries.XSound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

public class SoundUtils {

    private static FileConfiguration sounds;

    public static void sendSound(Player p, String configPath) {
        if (sounds == null) {
            File soundsFile = new File(ChickenUtils.getPlugin().getDataFolder(), "sounds.yml");
            sounds = YamlConfiguration.loadConfiguration(soundsFile);
        }

        String soundName = sounds.getString(configPath);
        if (soundName == null) return;
        if (soundName.isBlank()) return;

        SoundUtils.sendRawSound(p, soundName, 1, 1);
    }

    private static void sendRawSound(Player p, String soundName, double volume, double pitch) {
        try {
            XSound xSound = null;
            for (XSound sound : XSound.values()) {
                if (sound.name().equalsIgnoreCase(soundName)) {
                    xSound = sound;
                    break;
                }
            }

            if (xSound == null) return;

            xSound.play(p, (float) volume, (float) pitch);
        } catch (IllegalArgumentException e) {
            ChickenUtils.getPlugin().getLogger().severe("Error sound playing: " + soundName);
            e.printStackTrace();
        }

    }
}
