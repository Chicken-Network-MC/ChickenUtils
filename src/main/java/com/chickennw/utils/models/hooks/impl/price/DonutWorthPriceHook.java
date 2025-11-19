package com.chickennw.utils.models.hooks.impl.price;

import com.chickennw.utils.models.hooks.AbstractPluginHook;
import com.chickennw.utils.models.hooks.types.PriceHook;
import me.serbob.donutworth.api.util.Prices;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("unused")
public class DonutWorthPriceHook extends AbstractPluginHook implements PriceHook {

    public DonutWorthPriceHook() {
        super("DonutWorth Price Hook", true, "DonutWorth");
    }

    @Override
    public void load() {

    }

    @Override
    public void unload() {

    }

    @Override
    public boolean isEnabled() {
        return hooksYaml.getBoolean("price-hooks.donut-worth", false);
    }

    @Override
    public double calculatePrice(ItemStack item) {
        return Prices.getPrice(item);
    }

    @Override
    public double calculatePrice(ItemStack item, Player player) {
        return Prices.getPriceWithMultiplier(player, item);
    }
}
