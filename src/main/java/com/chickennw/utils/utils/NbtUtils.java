package com.chickennw.utils.utils;

import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class NbtUtils {

    public static ItemStack setKey(ItemStack itemStack, String key, String value) {
        NBTItem nbti = new NBTItem(itemStack);

        nbti.setString(key, value);
        return nbti.getItem();
    }

    public static String getValue(ItemStack itemStack, String key) {
        if (itemStack == null) return null;
        if (itemStack.getType() == Material.AIR) return null;
        if (itemStack.getAmount() < 1) return null;

        NBTItem nbti = new NBTItem(itemStack);
        if (nbti.hasTag(key)) return nbti.getString(key);
        else return null;
    }
}
