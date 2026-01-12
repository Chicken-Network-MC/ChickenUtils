package com.chickennw.utils.models.menus.triumph;

import com.chickennw.utils.ChickenUtils;
import com.chickennw.utils.configurations.menu.MenuFiller;
import com.chickennw.utils.configurations.menu.SymboledMenuItem;
import com.chickennw.utils.logger.LoggerFactory;
import com.chickennw.utils.managers.MenuManager;
import com.chickennw.utils.models.config.menu.TriumphMenuConfiguration;
import com.chickennw.utils.models.menus.triumph.item.TriumphItemHolder;
import com.chickennw.utils.utils.ChatUtils;
import com.chickennw.utils.utils.NbtUtils;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

@Getter
public abstract class TriumphMenuWrapper {

    protected static final String MENU_ACTION_NBT_KEY = "wrapped_menu_action";
    protected static final Logger logger = LoggerFactory.getLogger();

    protected final TriumphMenuConfiguration menuConfiguration;
    protected final MenuType menuType;
    protected BaseGui cachedGui;

    public TriumphMenuWrapper(TriumphMenuConfiguration menuConfiguration, MenuType menuType) {
        this.menuConfiguration = menuConfiguration;
        this.menuType = menuType;
    }

    public abstract Function<String, String> placeholderParser();

    public abstract Runnable nextPageRunnable();

    public abstract Runnable previousPageRunnable();

    public abstract HashMap<String, GuiAction<InventoryClickEvent>> premadeGuiActions();

    public abstract GuiAction<@NotNull InventoryClickEvent> getDefaultClickAction();

    public abstract GuiAction<@NotNull InventoryClickEvent> getDefaultTopClickAction();

    public abstract GuiAction<@NotNull InventoryCloseEvent> getCloseGuiAction();

    public void openAsync(Player player) {
        ChickenUtils.getFoliaLib().getScheduler().runAsync((task) -> {
            BaseGui baseGui = generateMenu(player);
            ChickenUtils.getFoliaLib().getScheduler().runNextTick((syncTask) -> baseGui.open(player));
        });
    }

    public BaseGui generateMenu(Player player) {
        initializeGuiObject();
        handleInteractions();
        fillGUI();
        createItems(player);

        return cachedGui;
    }

    public void update() {
        String invName = menuConfiguration.getTitle();
        invName = placeholderParser().apply(invName);
        invName = ChatUtils.colorizeLegacy(invName);
        cachedGui.updateTitle(Component.text(invName));
        cachedGui.update();
    }

    public void initializeGuiObject() {
        int rows = menuConfiguration.getPattern().size();
        if (menuType == MenuType.SIMPLE) {
            cachedGui = Gui.gui()
                    .title(Component.text(""))
                    .rows(rows)
                    .disableAllInteractions()
                    .create();
        } else if (menuType == MenuType.PAGINATED) {
            cachedGui = Gui.paginated()
                    .title(Component.text(""))
                    .rows(rows)
                    .create();
        }

        String invName = menuConfiguration.getTitle();
        invName = placeholderParser().apply(invName);
        invName = ChatUtils.colorizeLegacy(invName);
        cachedGui.updateTitle(Component.text(invName));
    }

    public void handleInteractions() {
        if (getDefaultClickAction() != null) cachedGui.setDefaultClickAction(getDefaultClickAction());
        if (getDefaultTopClickAction() != null) cachedGui.setDefaultTopClickAction(getDefaultTopClickAction());
        if (getCloseGuiAction() != null) cachedGui.setCloseGuiAction(getCloseGuiAction());
    }

    public void createItems(OfflinePlayer player) {
        for (TriumphItemHolder item : getMenuItems(player)) {
            item.slots().forEach(slot -> {
                cachedGui.setItem(slot, item.guiItem());
            });
        }
    }

    public List<TriumphItemHolder> getMenuItems(OfflinePlayer player) {
        List<TriumphItemHolder> items = new ArrayList<>();

        menuConfiguration.getItems().forEach((id, item) -> {
            try {
                if (item.getAction().equalsIgnoreCase("template")) return;

                TriumphItemHolder triumphItemHolder = generateItem(player, item, id);
                items.add(triumphItemHolder);
            } catch (Exception e) {
                logger.error("Got error while parsing menu item id: {}", id, e);
            }
        });

        return items;
    }

    public TriumphItemHolder generateItem(OfflinePlayer player, SymboledMenuItem symboledMenuItem, String id) {
        MenuManager menuManager = MenuManager.getInstance();
        ItemStack itemStack = menuManager.parseMenuItem(player, symboledMenuItem, placeholderParser());

        if (symboledMenuItem.getAction().equalsIgnoreCase("previous-page")) {
            GuiItem previousPageItem = ItemBuilder.from(itemStack).asGuiItem(event -> {
                if (cachedGui instanceof PaginatedGui paginatedGui) {
                    paginatedGui.previous();
                    previousPageRunnable().run();
                }
            });

            return new TriumphItemHolder(previousPageItem, parseSlots(symboledMenuItem.getSymbol()));
        } else if (symboledMenuItem.getAction().equalsIgnoreCase("next-page")) {
            GuiItem nextPageItem = ItemBuilder.from(itemStack).asGuiItem(event -> {
                if (cachedGui instanceof PaginatedGui paginatedGui) {
                    paginatedGui.next();
                    nextPageRunnable().run();
                }
            });

            return new TriumphItemHolder(nextPageItem, parseSlots(symboledMenuItem.getSymbol()));
        } else {
            GuiItem guiItem = ItemBuilder.from(itemStack).asGuiItem();
            setAction(symboledMenuItem.getAction(), guiItem);
            return new TriumphItemHolder(guiItem, parseSlots(symboledMenuItem.getSymbol()));
        }
    }

    public void setAction(String action, GuiItem guiItem) {
        GuiAction<InventoryClickEvent> func = premadeGuiActions().get(action);
        ItemStack itemStack = guiItem.getItemStack();
        itemStack = NbtUtils.setKey(itemStack, MENU_ACTION_NBT_KEY, action);

        if (func != null) {
            guiItem.setItemStack(itemStack);
            guiItem.setAction(func);
        }
    }

    public void fillGUI() {
        try {
            MenuFiller filler = menuConfiguration.getFiller();
            if (filler != null && filler.isEnabled()) {
                MenuManager menuManager = MenuManager.getInstance();
                ItemStack itemStack = menuManager.parseMenuItem(null, filler.getFillerItem(), null);
                cachedGui.getFiller().fill(new GuiItem(itemStack));
            }
        } catch (Exception e) {
            logger.error("Failed to fill GUI: {}", e.getMessage(), e);
        }
    }

    public List<Integer> parseSlots(char c) {
        List<Integer> slots = new ArrayList<>();
        int rowIndex = 0;

        for (String row : menuConfiguration.getPattern()) {
            String[] columns = row.split("");
            int columnIndex = 0;
            for (String col : columns) {
                char columnChar = col.charAt(0);
                if (columnChar == c) {
                    int slotIndex = rowIndex * 9 + columnIndex;
                    slots.add(slotIndex);
                }

                columnIndex++;
            }

            rowIndex++;
        }

        return slots;
    }
}
