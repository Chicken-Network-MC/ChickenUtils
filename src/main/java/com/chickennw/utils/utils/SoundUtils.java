package com.chickennw.utils.utils;

import com.chickennw.utils.ChickenUtils;
import com.cryptomorin.xseries.XSound;
import org.bukkit.Sound;
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

        sendSoundRaw(p, soundName);
    }

    public static void sendSoundRaw(Player p, String soundName) {
        try {
            XSound xSound = null;
            for (XSound sound : XSound.values()) {
                if (sound.name().equalsIgnoreCase(soundName)) {
                    xSound = sound;
                    break;
                }
            }

            if (xSound == null) return;

            SoundUtils.sendSound(p, xSound.get());
        } catch (IllegalArgumentException e) {
            ChickenUtils.getPlugin().getLogger().severe("Error sound playing: " + soundName);
            e.printStackTrace();
        }
    }

    public static void sendSound(Player p, Sound sound) {
        p.playSound(p, sound, 1, 1);
    }
}
