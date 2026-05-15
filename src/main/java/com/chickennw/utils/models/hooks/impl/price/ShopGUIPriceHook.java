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
        return hooksFile.getPriceHooks().isShopGuiPlus();
    }

    @Override
    public double calculateSellPrice(ItemStack item) {
        return ShopGuiPlusApi.getItemStackPriceSell(item);
    }

    @Override
    public double calculateSellPrice(ItemStack item, Player player) {
        return ShopGuiPlusApi.getItemStackPriceSell(player, item);
    }

    @Override
    public double calculateBuyPrice(ItemStack item) {
        return ShopGuiPlusApi.getItemStackPriceBuy(item);
    }

    @Override
    public double calculateBuyPrice(ItemStack item, Player player) {
        return ShopGuiPlusApi.getItemStackPriceBuy(player, item);
    }
}
