package com.chickennw.utils.models.hooks.impl.price;

import com.chickennw.utils.models.hooks.AbstractPluginHook;
import com.chickennw.utils.models.hooks.types.PriceHook;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("unused")
public class RoyaleEconomyHook extends AbstractPluginHook implements PriceHook {

    public RoyaleEconomyHook() {
        super("RoyaleEconomy Price Hook", true, "RoyaleEconomy");
    }

    @Override
    public void load() {

    }

    @Override
    public void unload() {

    }

    @Override
    public boolean isEnabled() {
        return hooksYaml.getBoolean("price-hooks.royale-economy-hook", true);
    }

    @Override
    public double calculatePrice(ItemStack item) {
        return RoyaleEconomy.apiHandler.shops.getPriceOfItem(item);
    }

    @Override
    public double calculatePrice(ItemStack item, Player player) {
        return RoyaleEconomy.apiHandler.shops.getPriceOfItem(item);
    }
}
