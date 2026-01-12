package com.chickennw.utils.configurations.menu;

import eu.okaeri.configs.annotation.NameModifier;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class MenuItem extends MenuItemStack {

    private List<Integer> slots;
    private String action;

    public MenuItem(String name, String material, int customModelData, List<String> lore, List<Integer> slots, String action) {
        super(name, material, customModelData, lore);
        this.slots = slots;
        this.action = action;
    }

    public MenuItem(MenuItemStack menuItemStack, List<Integer> slots, String action) {
        super(menuItemStack.getName(), menuItemStack.getMaterial(), menuItemStack.getCustomModelData(), menuItemStack.getLore());
        this.slots = slots;
        this.action = action;
    }
}
