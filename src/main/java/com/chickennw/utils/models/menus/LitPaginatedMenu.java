package com.chickennw.utils.models.menus;

import de.themoep.inventorygui.GuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.GuiPageElement;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public abstract class LitPaginatedMenu extends LitMenu {

    public LitPaginatedMenu(String name, Player player) {
        super(name, player);
    }

    public abstract List<LitMenuItemHolder> getTemplateItems();

    public GuiElementGroup getGuiElementGroup(List<LitMenuItemHolder> items) {
        String symbol = yaml.getString("items.template-item.symbol");
        if (symbol == null) {
            throw new NullPointerException("items.template-item.symbol is null");
        }

        char c = symbol.charAt(0);

        GuiElementGroup group = new GuiElementGroup(c);
        for (LitMenuItemHolder itemHolder : items) {
            group.addElement(itemHolder.getItem());
        }
        return group;
    }

    @Override
    public void createItems(UUID player) {
        elements.clear();

        for (String itemId : yaml.getConfigurationSection("items").getKeys(false)) {
            if (itemId.equalsIgnoreCase("template-item")) continue;

            LitMenuItemHolder guiElement = createItem(player, "items." + itemId);
            if (guiElement == null) continue;

            elements.add(guiElement.getItem());
        }

        List<LitMenuItemHolder> items = getTemplateItems();
        GuiElementGroup guiElementGroup = getGuiElementGroup(items);

        cachedGui.addElement(guiElementGroup);
        setFiller(cachedGui);
    }

    @Override
    public LitMenuItemHolder createItem(UUID player, String path) {
        LitMenuItemHolder item = super.createItem(player, path);
        item.setActionName(item.getActionName() + "_" + player.toString());

        String action = item.getActionName();
        if (action.equalsIgnoreCase("next_page")) {
            GuiElement current = item.getItem();
            GuiPageElement pageElement = new GuiPageElement(current.getSlotChar(), item.getItemStack(), GuiPageElement.PageAction.NEXT);
            return new LitMenuItemHolder(item.getActionName(), item.getItemStack(), pageElement);
        } else if (action.equalsIgnoreCase("previous_page")) {
            GuiElement current = item.getItem();
            GuiPageElement pageElement = new GuiPageElement(current.getSlotChar(), item.getItemStack(), GuiPageElement.PageAction.PREVIOUS);
            return new LitMenuItemHolder(item.getActionName(), item.getItemStack(), pageElement);
        }

        return item;
    }
}
