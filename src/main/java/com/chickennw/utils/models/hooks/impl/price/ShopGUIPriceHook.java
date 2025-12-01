package com.chickennw.utils.models.hooks.impl.price;

import com.chickennw.utils.models.hooks.AbstractPluginHook;
import com.chickennw.utils.models.hooks.types.PriceHook;
import net.brcdev.shopgui.ShopGuiPlusApi;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("unused")
public class ShopGUIPriceHook extends AbstractPluginHook implements PriceHook {

    public ShopGUIPriceHook() {
        super("ShopGUIPlus Price Hook", true, "ShopGUIPlus");
    }

    @Override
    public void load() {

    }

    @Override
    public void unload() {

    }

    @Override
    public boolean isEnabled() {
        return hooksYaml.getBoolean("price-hooks.shop-gui-plus", true);
    }

    @Override
    public double calculatePrice(ItemStack item) {
        return ShopGuiPlusApi.getItemStackPriceSell(item);
    }

    @Override
    public double calculatePrice(ItemStack item, Player player) {
        return ShopGuiPlusApi.getItemStackPriceSell(player, item);
    }
}
