package com.chickennw.utils.models.addons;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;

@RequiredArgsConstructor
@Getter
public abstract class PluginAddon {

    protected final Plugin plugin;

    public abstract void onLoad();

    public abstract void onEnable();

    public abstract void onDisable();

    public abstract String getAddonId();

    public abstract String getAddonName();

    public abstract String getAddonVersion();

    public abstract String getAddonDescription();

    public abstract String getAddonAuthor();
}
