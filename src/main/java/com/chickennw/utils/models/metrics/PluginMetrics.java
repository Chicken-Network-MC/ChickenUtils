package com.chickennw.utils.models.metrics;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.Plugin;

public class PluginMetrics extends Metrics {

    public PluginMetrics(Plugin plugin, int serviceId) {
        super(plugin, serviceId);
    }
}
