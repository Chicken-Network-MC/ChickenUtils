package com.chickennw.utils.models.hooks.types;

import com.chickennw.utils.models.hooks.PluginHook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface PriceHook extends PluginHook {

    double calculateSellPrice(ItemStack item, Player player);

    double calculateSellPrice(ItemStack item);

    double calculateBuyPrice(ItemStack item, Player player);

    double calculateBuyPrice(ItemStack item);

    @Deprecated
    default double calculatePrice(ItemStack item, Player player) {
        return calculateSellPrice(item, player);
    }

    @Deprecated
    default double calculatePrice(ItemStack item) {
        return calculateSellPrice(item);
    }
}
