package com.chickennw.utils.models.migrators;

import com.chickennw.utils.utils.ChatUtils;
import org.bukkit.command.CommandSender;

public class NotSupportedMigrator extends AbstractPluginMigrator {

    public NotSupportedMigrator(String pluginName) {
        super(pluginName, false);
    }

    @Override
    public void migrate(CommandSender sender) {
        ChatUtils.sendMessage(sender, "<red>You need to install " + pluginName + " plugin to use this migrator.");
    }
}
