package com.chickennw.utils.configurations.menu;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.NameModifier;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class SymboledMenuItem extends OkaeriConfig {

    private String name;
    private String material;
    private int customModelData;
    private List<String> lore;
    private String action;
    private char symbol;

    public SymboledMenuItem(String name, String material, int customModelData, List<String> lore, String action, char symbol) {
        this.name = name;
        this.material = material;
        this.customModelData = customModelData;
        this.lore = lore;
        this.symbol = symbol;
        this.action = action;
    }

    public SymboledMenuItem(MenuItemStack menuItemStack, String action, char symbol) {
        name = menuItemStack.getName();
        material = menuItemStack.getMaterial();
        customModelData = menuItemStack.getCustomModelData();
        lore = menuItemStack.getLore();
        this.symbol = symbol;
        this.action = action;
    }
}
