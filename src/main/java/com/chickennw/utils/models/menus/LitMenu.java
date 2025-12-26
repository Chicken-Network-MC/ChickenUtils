package com.chickennw.utils.models.menus;

import com.chickennw.utils.ChickenUtils;
import com.chickennw.utils.utils.ChatUtils;
import com.chickennw.utils.utils.ItemUtils;
import com.cryptomorin.xseries.XSound;
import com.tcoded.folialib.FoliaLib;
import de.themoep.inventorygui.GuiElement;
import de.themoep.inventorygui.GuiElement.Action;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.InventoryGui.CloseAction;
import de.themoep.inventorygui.StaticGuiElement;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

@Slf4j
@Getter
@Setter
public abstract class LitMenu {

    protected final JavaPlugin plugin;
    protected final String name;
    protected final Player player;
    protected final FileConfiguration yaml;
    protected final String[] pattern;
    protected final List<GuiElement> elements = new ArrayList<>();
    protected Sound sound;
    protected InventoryGui cachedGui;

    public LitMenu(String name, Player player) {
        plugin = ChickenUtils.getPlugin();
        this.name = name;
        this.player = player;

        File menuFile = getMenuFile();
        yaml = YamlConfiguration.loadConfiguration(menuFile);
        pattern = yaml.getStringList("pattern").toArray(new String[0]);

        String rawSound = yaml.getString("open_sound");
        try {
            if (rawSound == null || rawSound.equalsIgnoreCase("")) return;

            XSound.of(rawSound.toUpperCase(Locale.US))
                    .ifPresentOrElse(sound -> this.sound = sound.get(),
                            () -> plugin.getLogger().severe("Couldn't find sound named: " + rawSound + " in menu: " + this.name));
        } catch (Exception ex) {
            plugin.getLogger().severe("Couldn't find sound named: " + rawSound + " in menu: " + this.name);
        }
    }

    public abstract HashMap<String, Action> getGuiActions();

    public abstract String parsePlaceholder(String placeholder);

    public abstract List<String> parsePlaceholderAsList(String placeholder);

    public abstract CloseAction getCloseAction();

    public void setFiller(InventoryGui inventoryGui) {
        boolean enabled = yaml.getBoolean("filler.enabled");
        if (!enabled) return;

        ItemStack itemStack = createItemStack(player.getUniqueId(), "filler");
        inventoryGui.setFiller(itemStack);
    }

    public InventoryGui generateInventory(Player player) {
        String title = yaml.getString("title");
        InventoryGui gui = new InventoryGui(plugin, player, parse(title), pattern);
        gui.setCloseAction(getCloseAction());
        return gui;
    }

    public void openAsync(Player player) {
        FoliaLib foliaLib = ChickenUtils.getFoliaLib();
        foliaLib.getScheduler().runAsync((wrappedTask) -> {
            try {
                cachedGui = generateInventory(player);
                createItems(this.player.getUniqueId());

                foliaLib.getScheduler().runAtEntity(player, (playerTask) -> {
                    cachedGui.show(player);
                    if (sound != null) {
                        player.playSound(player.getLocation(), sound, 1f, 1f);
                    }
                });
            } catch (Exception ex) {
                log.error("Couldn't open menu named: {}", ex.getMessage(), ex);
            }
        });
    }

    public void openAsync(Player player, List<GuiElement> elements) {
        FoliaLib foliaLib = ChickenUtils.getFoliaLib();
        foliaLib.getScheduler().runAsync((wrappedTask) -> {
            try {
                cachedGui = generateInventory(player);
                elements.forEach(element -> cachedGui.addElement(element));
                setFiller(cachedGui);

                foliaLib.getScheduler().runAtEntity(player, (playerTask) -> {
                    cachedGui.show(player);
                    if (sound != null) {
                        player.playSound(player.getLocation(), sound, 1f, 1f);
                    }
                });
            } catch (Exception ex) {
                log.error("Couldn't open menu named: {}", ex.getMessage(), ex);
            }
        });
    }

    public void redraw(Player player) {
        createItems(this.player.getUniqueId());
        cachedGui.draw(player);
    }

    public void createItems(UUID uuid) {
        elements.clear();

        for (String itemId : yaml.getConfigurationSection("items").getKeys(false)) {
            LitMenuItemHolder guiElement = createItem(player.getUniqueId(), "items." + itemId);
            if (guiElement == null) continue;

            elements.add(guiElement.getItem());
        }

        elements.forEach(cachedGui::addElement);
        setFiller(cachedGui);
    }

    public LitMenuItemHolder createItem(UUID player, String path) {
        String rawSymbol = yaml.getString(path + ".symbol");
        if (rawSymbol == null || rawSymbol.equalsIgnoreCase(""))
            throw new RuntimeException("Invalid item with symbol path: " + path);

        char menuSymbol = rawSymbol.charAt(0);
        String actionName = yaml.getString(path + ".action");

        Action guiAction = getGuiActions().get(actionName);
        ItemStack itemStack = createItemStack(player, path);
        StaticGuiElement item = new StaticGuiElement(menuSymbol, itemStack, guiAction);
        return new LitMenuItemHolder(actionName, itemStack, item);
    }

    public ItemStack parseRawItem(UUID player, String path) {
        String rawMaterial = yaml.getString(path + ".material");
        return ItemUtils.parseItemStack(rawMaterial, player).join();
    }

    public ItemStack createItemStack(UUID player, String path) {
        ItemStack itemStack = parseRawItem(player, path);
        if (itemStack.getType() == Material.AIR) return itemStack;

        String itemName = yaml.getString(path + ".name");
        itemName = parse(itemName);

        List<String> rawLore = yaml.getStringList(path + ".lore");
        List<String> lore = new ArrayList<>();
        rawLore.forEach(string -> lore.addAll(parsePlaceholderAsList(parse(string))));

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (!itemName.equalsIgnoreCase("none")) itemMeta.setDisplayName(itemName);
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    public File getMenuFile() {
        return new File(plugin.getDataFolder(), String.format("menu/%s.yml", name));
    }

    public String parse(String string) {
        string = parsePlaceholder(string);
        return ChatUtils.colorizeLegacy(string);
    }
}
