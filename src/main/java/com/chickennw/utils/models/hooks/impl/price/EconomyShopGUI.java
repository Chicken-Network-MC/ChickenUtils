package com.chickennw.utils.models.hooks.impl.price;

import com.chickennw.utils.models.hooks.AbstractPluginHook;
import com.chickennw.utils.models.hooks.types.PriceHook;
import me.gypopo.economyshopgui.api.EconomyShopGUIHook;
import me.gypopo.economyshopgui.api.objects.SellPrice;
import me.gypopo.economyshopgui.objects.ShopItem;
import me.gypopo.economyshopgui.util.EconomyType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
public class EconomyShopGUI extends AbstractPluginHook implements PriceHook {

    public EconomyShopGUI() {
        super("EconomyShopGUI Price Hook", true, List.of("EconomyShopGUI", "EconomyShopGUI-Premium"));
    }

    @Override
    public void load() {

    }

    @Override
    public void unload() {

    }

    @Override
    public boolean isEnabled() {
        return hooksYaml.getBoolean("price-hooks.economy-shop-gui", true);
    }

    @Override
    public double calculatePrice(ItemStack item) {
        ShopItem shopItem = EconomyShopGUIHook.getShopItem(item);

        if (shopItem != null) {
            Double price = EconomyShopGUIHook.getItemSellPrice(shopItem, item);
            if (price != null) {
                return price;
            }
        }

        return 0;
    }

    @Override
    public double calculatePrice(ItemStack item, Player player) {
        Optional<SellPrice> optSellPrice = EconomyShopGUIHook.getSellPrice(player, item);
        return optSellPrice.map(sellPrice -> sellPrice.getPrice(EconomyType.getFromString("VAULT"))).orElse((double) 0);
    }
}
