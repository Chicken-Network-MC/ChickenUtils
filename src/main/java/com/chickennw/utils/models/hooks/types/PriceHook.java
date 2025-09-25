package com.chickennw.utils.models.hooks.types;

import com.chickennw.utils.models.hooks.PluginHook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface PriceHook extends PluginHook {

    double calculatePrice(ItemStack item, Player player);

    double calculatePrice(ItemStack item);
}
