package com.chickennw.utils.models.hooks;

import com.chickennw.utils.ChickenUtils;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

@Getter
public abstract class AbstractPluginHook implements PluginHook {

    protected final String name;
    protected final boolean requirePlugin;
    protected final List<String> requiredPlugins;
    protected final FileConfiguration hooksYaml;

    public AbstractPluginHook(String name, boolean requirePlugin, String requiredPlugin) {
        this.name = name;
        this.requirePlugin = requirePlugin;
        requiredPlugins = List.of(requiredPlugin);

        File hooksFile = new File(ChickenUtils.getPlugin().getDataFolder(), "hooks.yml");
        hooksYaml = YamlConfiguration.loadConfiguration(hooksFile);
    }

    public AbstractPluginHook(String name, boolean requirePlugin, List<String> requiredPlugins) {
        this.name = name;
        this.requirePlugin = requirePlugin;
        this.requiredPlugins = requiredPlugins;

        File hooksFile = new File(ChickenUtils.getPlugin().getDataFolder(), "hooks.yml");
        hooksYaml = YamlConfiguration.loadConfiguration(hooksFile);
    }

    @Override
    public abstract void load();

    @Override
    public abstract void unload();

    public abstract boolean isEnabled();
}
