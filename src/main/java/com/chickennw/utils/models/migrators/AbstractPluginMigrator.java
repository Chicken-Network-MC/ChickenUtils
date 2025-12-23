package com.chickennw.utils.models.migrators;

import com.chickennw.utils.utils.ChatUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

@Getter
@RequiredArgsConstructor
public abstract class AbstractPluginMigrator {

    protected final String pluginName;
    protected final boolean requirePlugin;

    public abstract void migrate(CommandSender sender);

    public boolean isMigratable() {
        if (requirePlugin) return Bukkit.getPluginManager().getPlugin(pluginName) != null;
        return true;
    }

    public void onBatchDone(CommandSender sender, int migratedCount, int totalCount) {
        ChatUtils.sendMessage(sender, "<green>Migrated " + migratedCount + " out of " +
                totalCount + " entries from " + pluginName + " plugin.");
    }

    public void onDone(CommandSender sender, int totalCount) {
        ChatUtils.sendMessage(sender, "<green>Migrated " + totalCount + " entries from " + pluginName + " plugin.");
        ChatUtils.sendMessage(sender, "<green>Delete the " + pluginName + " plugin now and restart the server.");
    }
}
