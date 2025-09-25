package com.chickennw.utils.models.hooks.impl.other;

import com.chickennw.utils.models.hooks.types.OtherHook;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class PlaceholderApiHook extends PlaceholderExpansion implements OtherHook {

    @Override
    public @NotNull String getIdentifier() {
        return "litspawners";
    }

    @Override
    public @NotNull String getAuthor() {
        return "WaterArchery";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public void load() {

    }

    @Override
    public void unload() {

    }
}
