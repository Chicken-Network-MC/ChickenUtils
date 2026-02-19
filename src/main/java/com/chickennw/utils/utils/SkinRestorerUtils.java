package com.chickennw.utils.utils;

import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.api.storage.PlayerStorage;

import java.util.Optional;
import java.util.UUID;

public class SkinRestorerUtils {

    private static SkinsRestorer skinsRestorerAPI;

    public static void init() {
        skinsRestorerAPI = SkinsRestorerProvider.get();
    }

    public static SkinProperty getPlayerSkin(UUID uuid, String playerName) {
        try {
            PlayerStorage playerStorage = skinsRestorerAPI.getPlayerStorage();
            Optional<SkinProperty> property = playerStorage.getSkinForPlayer(uuid, playerName);
            return property.orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}
