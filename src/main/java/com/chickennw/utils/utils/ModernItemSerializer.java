package com.chickennw.utils.utils;

import org.bukkit.inventory.ItemStack;

import java.util.Base64;

public class ModernItemSerializer {

    public static String serialize(ItemStack item) {
        return Base64.getEncoder().encodeToString(item.serializeAsBytes());
    }

    public static ItemStack deserialize(String data) {
        byte[] bytes = Base64.getDecoder().decode(data);
        return ItemStack.deserializeBytes(bytes);
    }

}
