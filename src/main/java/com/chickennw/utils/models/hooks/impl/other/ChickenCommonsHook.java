package com.chickennw.utils.models.hooks.impl.other;

import com.chickennw.utils.models.hooks.AbstractPluginHook;
import com.chickennw.utils.models.hooks.types.OtherHook;
import me.waterarchery.chickencommons.handlers.CacheHandler;

import java.util.UUID;

@SuppressWarnings("unused")
public class ChickenCommonsHook extends AbstractPluginHook implements OtherHook {

    private CacheHandler cacheHandler;

    public ChickenCommonsHook() {
        super("ChickenCommons Hook", true, "ChickenCommons");
    }

    @Override
    public void load() {
        cacheHandler = CacheHandler.getInstance();
    }

    @Override
    public void unload() {

    }

    @Override
    public boolean isEnabled() {
        return hooksYaml.getBoolean("other-hooks.chicken-commons", true);
    }

    public boolean isClientPlayer(UUID uuid) {
        return cacheHandler.getClientPlayer(uuid) != null;
    }
}
