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

	public static String toBase64(ItemStack item) {
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			 BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {

			dataOutput.writeObject(item);
			return Base64.getEncoder().encodeToString(outputStream.toByteArray());

		} catch (IOException e) {
			throw new RuntimeException("Unable to serialize ItemStack", e);
		}
	}

	public static ItemStack fromBase64(String base64) {
		try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
			 BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {

			return (ItemStack) dataInput.readObject();
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException("Unable to deserialize ItemStack", e);
		}
	}
}
