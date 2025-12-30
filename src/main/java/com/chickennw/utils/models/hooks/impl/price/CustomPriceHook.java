package com.chickennw.utils.models.hooks.impl.price;

import com.chickennw.utils.configurations.CustomWorthFile;
import com.chickennw.utils.managers.ConfigManager;
import com.chickennw.utils.models.hooks.AbstractPluginHook;
import com.chickennw.utils.models.hooks.types.PriceHook;
import com.chickennw.utils.utils.ConfigUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;

@SuppressWarnings("unused")
public class CustomPriceHook extends AbstractPluginHook implements PriceHook {

    private final HashMap<Material, Double> customPrices = new HashMap<>();

    public CustomPriceHook() {
        super("Custom Price Hook", false, List.of("Vault"));
    }

    @Override
    public void load() {
        ConfigManager configManager = ConfigManager.getInstance();
        configManager.loadOkaeriConfig(CustomWorthFile.class);
    }

    @Override
    public void unload() {

    }

    @Override
    public boolean isEnabled() {
        return hooksFile.getPriceHooks().isCustomWorthFile();
    }

    @Override
    public double calculatePrice(ItemStack item) {
        Material material = item.getType();
        CustomWorthFile customWorthFile = ConfigUtils.get(CustomWorthFile.class);
        Double price = customWorthFile.getWorths().get(material);
        return price != null ? price * item.getAmount() : 0.0;
    }

    @Override
    public double calculatePrice(ItemStack item, Player player) {
        return calculatePrice(item);
    }
}
