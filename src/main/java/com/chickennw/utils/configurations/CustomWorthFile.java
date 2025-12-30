package com.chickennw.utils.configurations;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.NameModifier;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;

import java.util.Map;

@Getter
@Setter
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class CustomWorthFile extends OkaeriConfig {

    Map<Material, Double> worths = Map.of(
            Material.DIAMOND, 1D,
            Material.GOLD_INGOT, 1D,
            Material.IRON_INGOT, 0.4D,
            Material.EMERALD, 2D,
            Material.COAL, 0.1D,
            Material.COPPER_INGOT, 0.2D,
            Material.WHEAT, 0.05D,
            Material.CARROT, 0.05D,
            Material.POTATO, 0.05D);
}
