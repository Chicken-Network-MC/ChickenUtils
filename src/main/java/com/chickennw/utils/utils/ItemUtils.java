package com.chickennw.utils.utils;

import com.chickennw.api.utils.HeadUtils;
import com.chickennw.utils.ChickenUtils;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.profiles.builder.XSkull;
import com.cryptomorin.xseries.profiles.objects.Profileable;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ItemUtils {

    private static final ConcurrentHashMap<String, ItemStack> cachedPlayerHeads = new ConcurrentHashMap<>();

    public static CompletableFuture<ItemStack> parseItemStack(String rawMaterial, UUID player) {
        if (rawMaterial == null) return CompletableFuture.completedFuture(new ItemStack(Material.STONE));

        if (rawMaterial.contains("HEAD-")) {
            rawMaterial = rawMaterial.replace("HEAD-", "");
            return HeadUtils.getHead(rawMaterial);
        } else if (rawMaterial.equalsIgnoreCase("player")) {
            return HeadUtils.getHead(player);
        } else {
            Optional<XMaterial> optMaterial = XMaterial.matchXMaterial(rawMaterial);
            XMaterial material = optMaterial.orElse(XMaterial.STONE);
            return CompletableFuture.completedFuture(material.parseItem());
        }
    }

    public static CompletableFuture<ItemStack> parseItemStack(String rawMaterial, OfflinePlayer player) {
        if (rawMaterial == null) return CompletableFuture.completedFuture(new ItemStack(Material.STONE));

        if (rawMaterial.equalsIgnoreCase("player")) return HeadUtils.getHead(player);
        return ItemUtils.parseItemStack(rawMaterial, player.getUniqueId());
    }

    public static ItemStack parseTexture(String texture) {
        return ItemUtils.getCachedItem(texture);
    }

    public static Material parseMaterial(String name) {
        try {
            return Material.matchMaterial(name);
        } catch (Exception e) {
            ChickenUtils.getPlugin().getLogger().severe("Invalid material name: " + name);
            return Material.STONE;
        }
    }

    private static ItemStack getCachedItem(String key) {
        ItemStack item = cachedPlayerHeads.get(key);

        if (item == null) {
            try {
                item = XSkull.createItem().profile(Profileable.detect(key)).apply();
                cachedPlayerHeads.put(key, item);
            } catch (Exception e) {
                return new ItemStack(Material.PLAYER_HEAD);
            }
        }

        return item;
    }
}
