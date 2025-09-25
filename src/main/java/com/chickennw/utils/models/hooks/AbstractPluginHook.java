package com.chickennw.utils.models.hooks;

import com.chickennw.utils.ChickenUtils;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

@Getter
public abstract class AbstractPluginHook implements PluginHook {

    protected final String name;
    protected final boolean requirePlugin;
    protected final String requiredPlugin;
    protected final FileConfiguration hooksYaml;

    public AbstractPluginHook(String name, boolean requirePlugin, String requiredPlugin) {
        this.name = name;
        this.requirePlugin = requirePlugin;
        this.requiredPlugin = requiredPlugin;

        File hooksFile = new File(ChickenUtils.getPlugin().getDataFolder(), "hooks.yml");
        hooksYaml = YamlConfiguration.loadConfiguration(hooksFile);
    }

    @Override
    public abstract void load();

    @Override
    public abstract void unload();

    public abstract boolean isEnabled();
}
