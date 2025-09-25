package com.chickennw.utils.models.config;

import eu.okaeri.configs.OkaeriConfig;

public record ConfigClassHolder(OkaeriConfig config, Class<? extends OkaeriConfig> configClass) {

}
