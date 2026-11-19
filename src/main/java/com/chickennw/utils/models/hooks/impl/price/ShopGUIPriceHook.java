package com.chickennw.utils.models.hooks.impl.price;

import com.chickennw.utils.models.hooks.AbstractPluginHook;
import com.chickennw.utils.models.hooks.types.PriceHook;
import net.brcdev.shopgui.ShopGuiPlusApi;
import net.brcdev.shopgui.exception.shop.ShopsNotLoadedException;
import net.brcdev.shopgui.shop.item.ShopItem;
import net.brcdev.shopgui.shop.item.ShopItemType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.Predicate;

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
        double price = ShopGuiPlusApi.getItemStackPriceSell(item);
        if (price > 0) return price;
        ShopItem shopItem = findItem(item, candidate -> candidate.getSellPrice() > 0);
        return shopItem == null ? price : shopItem.getSellPriceForAmount(item.getAmount());
    }

    @Override
    public double calculateSellPrice(ItemStack item, Player player) {
        double price = ShopGuiPlusApi.getItemStackPriceSell(player, item);
        if (price > 0) return price;
        ShopItem shopItem = findItem(item, candidate -> candidate.getSellPrice() > 0);
        return shopItem == null ? price : shopItem.getSellPriceForAmount(player, item.getAmount());
    }

    @Override
    public double calculateBuyPrice(ItemStack item) {
        double price = ShopGuiPlusApi.getItemStackPriceBuy(item);
        if (price > 0) return price;
        ShopItem shopItem = findItem(item, candidate -> candidate.getBuyPrice() > 0);
        return shopItem == null ? price : shopItem.getBuyPriceForAmount(item.getAmount());
    }

    @Override
    public double calculateBuyPrice(ItemStack item, Player player) {
        double price = ShopGuiPlusApi.getItemStackPriceBuy(player, item);
        if (price > 0) return price;
        ShopItem shopItem = findItem(item, candidate -> candidate.getBuyPrice() > 0);
        return shopItem == null ? price : shopItem.getBuyPriceForAmount(player, item.getAmount());
    }

    private ShopItem findItem(ItemStack item, Predicate<ShopItem> priced) {
        try {
            return ShopGuiPlusApi.getPlugin().getShopManager().getShops().stream()
                .flatMap(shop -> shop.getShopItems().stream())
                .filter(candidate -> candidate.getType() == ShopItemType.ITEM && priced.test(candidate) && candidate.getItem().isSimilar(item))
                .findFirst()
                .orElse(null);
        } catch (ShopsNotLoadedException exception) {
            return null;
        }
    }
}
