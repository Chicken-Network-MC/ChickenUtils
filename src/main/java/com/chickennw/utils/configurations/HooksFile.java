package com.chickennw.utils.configurations;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.NameModifier;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class HooksFile extends OkaeriConfig {

    private ConfigPriceHook priceHooks = new ConfigPriceHook();

    private ConfigHologramHook hologramHooks = new ConfigHologramHook();

    @Getter
    @Setter
    public static class ConfigPriceHook extends OkaeriConfig {
        private boolean essentials = true;

        private boolean shopGuiPlus = true;

        private boolean donutWorth = true;

        private boolean royaleEconomy = true;

        private boolean economyShopGui = true;

        private boolean customWorthFile = false;
    }

    @Getter
    @Setter
    public static class ConfigHologramHook extends OkaeriConfig {
        private boolean cmi = true;

        private boolean decentHolograms = true;

        private boolean fancyHolograms = true;
    }
}
