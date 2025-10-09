package com.chickennw.utils.utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

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
