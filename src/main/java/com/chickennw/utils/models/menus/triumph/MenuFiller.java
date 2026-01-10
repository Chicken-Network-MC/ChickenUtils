package com.chickennw.utils.models.menus.triumph;

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
    private String material = "GRAY_STAINED_GLASS_PANE";
    private int customModelData = 0;
    private String name ="<gray>";
    private List<String> lore = List.of();
}

