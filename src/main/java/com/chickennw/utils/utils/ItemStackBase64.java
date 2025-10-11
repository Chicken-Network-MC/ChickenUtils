package com.chickennw.utils.utils;

import org.bukkit.inventory.ItemStack;

/**
 * Should probably switch to a more robust serialization method in the future using ItemStack#serializeAsBytes()
 * BUT as far as I know, it does not support upgrading the server version without breaking items.
 */
public class ItemStackBase64 {

    public static byte[] toBase64(ItemStack item) {
        return item.serializeAsBytes();
    }

    public static ItemStack fromBase64(byte[] bytes) {
        return ItemStack.deserializeBytes(bytes);
    }
}
