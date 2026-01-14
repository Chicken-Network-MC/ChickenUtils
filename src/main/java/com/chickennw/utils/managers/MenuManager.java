package com.chickennw.utils.managers;

import com.chickennw.utils.configurations.menu.MenuItemStack;
import com.chickennw.utils.configurations.menu.SymboledMenuItem;
import com.chickennw.utils.utils.ChatUtils;
import com.chickennw.utils.utils.ItemUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class MenuManager {

    private static MenuManager instance;

    public static MenuManager getInstance() {
        if (instance == null) {
            instance = new MenuManager();
        }
        return instance;
    }

    private MenuManager() {

    }

    public ItemStack parseMenuItem(OfflinePlayer offlinePlayer, MenuItemStack menuItem, Function<String, String> placeholderParser) {
        return parseMenuItem(offlinePlayer, menuItem.getMaterial(), menuItem.getName(), menuItem.getLore(),
                menuItem.getCustomModelData(), placeholderParser);
    }

    public ItemStack parseMenuItem(OfflinePlayer offlinePlayer, SymboledMenuItem menuItem, Function<String, String> placeholderParser) {
        return parseMenuItem(offlinePlayer, menuItem.getMaterial(), menuItem.getName(), menuItem.getLore(),
                menuItem.getCustomModelData(), placeholderParser);
    }

    public ItemStack parseMenuItem(OfflinePlayer offlinePlayer, String material, String name, List<String> lore,
                                   int customModelData, Function<String, String> placeholderParser) {
        ItemStack itemStack = ItemUtils.parseItemStack(material).join();
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (placeholderParser != null) name = placeholderParser.apply(name);
        itemMeta.setDisplayName(ChatUtils.colorizeLegacy(name));

        List<String> newLore = new ArrayList<>();
        lore.forEach(p -> {
            if (placeholderParser != null) p = placeholderParser.apply(p);
            newLore.add(ChatUtils.colorizeLegacy(p));
        });
        itemMeta.setLore(newLore);

        if (customModelData != -1) itemMeta.setCustomModelData(customModelData);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
