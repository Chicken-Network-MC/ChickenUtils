package com.chickennw.utils.models.hooks.impl.price;

import com.chickennw.utils.models.hooks.AbstractPluginHook;
import com.chickennw.utils.models.hooks.types.PriceHook;
import com.earth2me.essentials.Essentials;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;

@SuppressWarnings("unused")
public class EssentialsPriceHook extends AbstractPluginHook implements PriceHook {

    public EssentialsPriceHook() {
        super("Essentials Price Hook", true, "Essentials");
    }

    @Override
    public void load() {

    }

    @Override
    public void unload() {

    }

    @Override
    public boolean isEnabled() {
        return hooksYaml.getBoolean("price-hooks.essentials", true);
    }

    public double calculatePrice(ItemStack item) {
        Essentials essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
        BigDecimal price = essentials.getWorth().getPrice(essentials, item);
        if (price == null) {
            return 0;
        } else {
            return price.doubleValue() * item.getAmount();
        }
    }

    @Override
    public double calculatePrice(ItemStack item, Player player) {
        return calculatePrice(item);
    }
}
