package com.chickennw.utils.utils;

import com.chickennw.utils.database.sql.Database;
import com.chickennw.utils.models.config.head.HeadEntity;
import com.cryptomorin.xseries.profiles.builder.XSkull;
import com.cryptomorin.xseries.profiles.objects.ProfileInputType;
import com.cryptomorin.xseries.profiles.objects.Profileable;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.hibernate.Session;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class HeadUtils {

    private static final ConcurrentHashMap<UUID, ItemStack> headsWithId = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, ItemStack> headsWithTexture = new ConcurrentHashMap<>();
    private static final List<HeadEntity> cachedHeads = new ArrayList<>();
    private static Database database;

    public static void init(Database db) {
        database = db;
        try (Session session = database.getSessionFactory().openSession()) {
            List<HeadEntity> heads = session.createQuery("from HeadEntity", HeadEntity.class).list();
            cachedHeads.addAll(heads);
        }
    }

    public static CompletableFuture<ItemStack> getHead(UUID uuid) {
        ItemStack itemStack = HeadUtils.getHeadCache(uuid);
        if (itemStack != null) {
            return CompletableFuture.completedFuture(itemStack);
        }

        return XSkull.createItem()
                .profile(Profileable.of(uuid))
                .applyAsync()
                .orTimeout(3, TimeUnit.SECONDS)
                .whenComplete((head, throwable) -> {
                    if (head == null) head = new ItemStack(Material.PLAYER_HEAD);
                    HeadUtils.cache(uuid, head);
                }).exceptionally(t -> {
                    ItemStack fallbackHead = new ItemStack(Material.PLAYER_HEAD);
                    HeadUtils.cache(uuid, fallbackHead);
                    return fallbackHead.clone();
                });
    }

    public static CompletableFuture<ItemStack> getHead(String texture) {
        ItemStack itemStack = HeadUtils.getHeadCache(texture);
        if (itemStack != null) {
            return CompletableFuture.completedFuture(itemStack);
        }

        ProfileInputType type = texture.startsWith("ey") ? ProfileInputType.BASE64 : ProfileInputType.TEXTURE_URL;
        CompletableFuture<ItemStack> headFuture = XSkull.createItem()
                .profile(Profileable.of(type, texture))
                .applyAsync()
                .orTimeout(3, TimeUnit.SECONDS);
        headFuture.whenComplete((head, throwable) -> {
            HeadUtils.cache(texture, head);
        });

        return headFuture;
    }

    public static CompletableFuture<ItemStack> getHead(OfflinePlayer player) {
        ItemStack itemStack = HeadUtils.getHeadCache(player.getUniqueId());
        if (itemStack != null) {
            return CompletableFuture.completedFuture(itemStack);
        }

        return CompletableFuture.supplyAsync(() -> {
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
            skullMeta.setOwningPlayer(player);
            skull.setItemMeta(skullMeta);
            HeadUtils.cache(player.getUniqueId(), skull);
            return skull.clone();
        }).orTimeout(3, TimeUnit.SECONDS).exceptionally(t -> {
            ItemStack fallbackHead = new ItemStack(Material.PLAYER_HEAD);
            HeadUtils.cache(player.getUniqueId(), fallbackHead);
            return fallbackHead.clone();
        });
    }

    private static void cache(UUID uuid, ItemStack itemStack) {
        ProfileProperty textureProperty = getTextureProperty(itemStack);
        if (textureProperty != null) {
            String textureValue = textureProperty.getValue();
            headsWithTexture.put(textureValue, itemStack);

            HeadEntity headEntity = new HeadEntity(uuid, textureValue);
            cachedHeads.add(headEntity);

            database.save(headEntity);
        }

        headsWithId.put(uuid, itemStack);
    }

    private static void cache(String texture, ItemStack itemStack) {
        headsWithTexture.put(texture, itemStack);
    }

    @Nullable
    private static ItemStack getHeadCache(UUID uuid) {
        ItemStack itemStack = headsWithId.get(uuid);
        if (itemStack != null) return itemStack.clone();

        HeadEntity headEntity = cachedHeads.stream().filter(h -> h.getUuid().equals(uuid)).findFirst().orElse(null);
        if (headEntity != null) {
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
            PlayerProfile playerProfile = Bukkit.createProfile(uuid, null);
            ProfileProperty profileProperty = new ProfileProperty("textures", headEntity.getTexture());

            playerProfile.setProperty(profileProperty);
            skullMeta.setPlayerProfile(playerProfile);
            skull.setItemMeta(skullMeta);
            headsWithId.put(uuid, skull);
            return skull.clone();
        }

        return null;
    }

    @Nullable
    private static ItemStack getHeadCache(String texture) {
        ItemStack itemStack = headsWithTexture.get(texture);
        if (itemStack != null) return itemStack.clone();

        return null;
    }

    private static ProfileProperty getTextureProperty(ItemStack itemStack) {
        SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
        PlayerProfile profile = skullMeta.getPlayerProfile();
        if (profile == null) return null;

        return profile.getProperties()
                .stream()
                .filter(p -> p.getName().equals("textures"))
                .findFirst()
                .orElse(null);
    }
}
