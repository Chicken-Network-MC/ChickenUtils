package com.chickennw.utils.models.menus.triumph;

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

    private int slot;
    private String action;

    public MenuItem(String name, String material, int customModelData, List<String> lore, int slot, String action) {
        super(name, material, customModelData, lore);
        this.slot = slot;
        this.action = action;
    }
}
