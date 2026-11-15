package com.chickennw.utils.models.hooks.impl.price;

import com.chickennw.utils.models.hooks.AbstractPluginHook;
import com.chickennw.utils.models.hooks.types.PriceHook;
import me.waterarchery.worth.api.LitWorthAPI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("unused")
public class LitWorthPriceHook extends AbstractPluginHook implements PriceHook {

    public LitWorthPriceHook() {
        super("LitWorth Price Hook", true, "LitWorth");
    }

    @Override
    public void load() {

    }

    @Override
    public void unload() {

    }

    @Override
    public boolean isEnabled() {
        return hooksFile.getPriceHooks().isLitWorth();
    }

    @Override
    public double calculateSellPrice(ItemStack item) {
        return LitWorthAPI.getWorth(item);
    }

    @Override
    public double calculateSellPrice(ItemStack item, Player player) {
        return LitWorthAPI.getWorth(player, item);
    }

    @Override
    public double calculateBuyPrice(ItemStack item) {
        return LitWorthAPI.getWorth(item);
    }

    @Override
    public double calculateBuyPrice(ItemStack item, Player player) {
        return LitWorthAPI.getWorth(player, item);
    }
}
