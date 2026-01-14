package com.chickennw.utils.configurations.menu;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.NameModifier;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class MenuFiller extends OkaeriConfig {

    private boolean enabled = true;
    private MenuItemStack fillerItem = new MenuItemStack(" ", "BLACK_STAINED_GLASS_PANE", -1, List.of());
}

