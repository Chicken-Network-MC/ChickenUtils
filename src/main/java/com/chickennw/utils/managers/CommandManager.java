package com.chickennw.utils.managers;

import com.chickennw.utils.ChickenUtils;
import com.chickennw.utils.logger.LoggerFactory;
import com.chickennw.utils.models.commands.BaseCommand;
import com.chickennw.utils.utils.ChatUtils;
import com.chickennw.utils.utils.SoundUtils;
import dev.triumphteam.cmd.bukkit.BukkitCommandManager;
import dev.triumphteam.cmd.bukkit.message.BukkitMessageKey;
import dev.triumphteam.cmd.core.exceptions.CommandRegistrationException;
import dev.triumphteam.cmd.core.message.MessageKey;
import dev.triumphteam.cmd.core.suggestion.SuggestionKey;
import dev.triumphteam.cmd.core.suggestion.SuggestionResolver;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommandManager {

    private static CommandManager instance;
    private final List<BaseCommand> commands = new ArrayList<>();
    private final Logger logger;
    private final BukkitCommandManager<CommandSender> manager;
    private final JavaPlugin plugin;
    private final FileConfiguration lang;

    public static CommandManager getInstance() {
        if (instance == null) {
            instance = new CommandManager();
        }

        return instance;
    }

    private CommandManager() {
        logger = LoggerFactory.getLogger();
        plugin = ChickenUtils.getPlugin();

        manager = BukkitCommandManager.create(plugin);

        File langFile = new File(plugin.getDataFolder(), "Lang.yml");
        lang = YamlConfiguration.loadConfiguration(langFile);

        registerSuggestions();
        registerMessages();
    }

    public <T extends BaseCommand> void registerCommand(T command) {
        logger.info("Registering command: {}", command.getClass().getSimpleName());
        manager.registerCommand(command);
        commands.add(command);
    }

    public void unregisterCommands() {
        commands.forEach(c -> {
            logger.info("Unregistering command: {}", c.getClass().getSimpleName());
            manager.unregisterCommand(c);
            c.getAlias().forEach(this::unregisterCommand);
            unregisterCommand(c.getCommand());
        });
    }

    private void unregisterCommand(String name) {
        getBukkitCommands(getCommandMap()).remove(name);
    }

    @NotNull
    private CommandMap getCommandMap() {
        try {
            final Server server = Bukkit.getServer();
            final Method getCommandMap = server.getClass().getDeclaredMethod("getCommandMap");
            getCommandMap.setAccessible(true);

            return (CommandMap) getCommandMap.invoke(server);
        } catch (final Exception ignored) {
            throw new CommandRegistrationException("Unable get Command Map. Commands will not be registered!");
        }
    }

    @NotNull
    private Map<String, org.bukkit.command.Command> getBukkitCommands(@NotNull final CommandMap commandMap) {
        try {
            final Field bukkitCommands = SimpleCommandMap.class.getDeclaredField("knownCommands");
            bukkitCommands.setAccessible(true);
            //noinspection unchecked
            return (Map<String, org.bukkit.command.Command>) bukkitCommands.get(commandMap);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new CommandRegistrationException("Unable get Bukkit commands. Commands might not be registered correctly!");
        }
    }

    public void registerSuggestion(SuggestionKey key, @NotNull SuggestionResolver.Simple<CommandSender> suggestionResolver) {
        manager.registerSuggestion(key, suggestionResolver);
    }

    private void registerSuggestions() {
        registerSuggestion(SuggestionKey.of("players"), (sender) -> {
            List<String> players = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                players.add(player.getName());
            }
            return players;
        });

        registerSuggestion(SuggestionKey.of("amount"), (sender) -> List.of("amount"));

        registerSuggestion(SuggestionKey.of("double-suggestion"), (sender) -> List.of("10.0", "20.0", "30.0"));
    }

    private void registerMessages() {
        FileConfiguration config = plugin.getConfig();
        String prefix = config.getString("prefix");

        manager.registerMessage(MessageKey.NOT_ENOUGH_ARGUMENTS, (sender, context) -> {
            ChatUtils.sendMessage(sender, ChatUtils.colorize(prefix + lang.getString("too-few-args")));
            if (sender instanceof Player player) SoundUtils.sendSound(player, "invalid-command");
        });

        manager.registerMessage(MessageKey.TOO_MANY_ARGUMENTS, (sender, context) -> {
            ChatUtils.sendMessage(sender, ChatUtils.colorize(prefix + lang.getString("too-many-args")));
            if (sender instanceof Player player) SoundUtils.sendSound(player, "invalid-command");
        });

        manager.registerMessage(MessageKey.INVALID_ARGUMENT, (sender, context) -> {
            ChatUtils.sendMessage(sender, ChatUtils.colorize(prefix + lang.getString("invalid-arg")));
            if (sender instanceof Player player) SoundUtils.sendSound(player, "invalid-command");
        });

        manager.registerMessage(MessageKey.UNKNOWN_COMMAND, (sender, context) -> {
            ChatUtils.sendMessage(sender, ChatUtils.colorize(prefix + lang.getString("unknown-command")));
            if (sender instanceof Player player) SoundUtils.sendSound(player, "invalid-command");
        });

        manager.registerMessage(BukkitMessageKey.NO_PERMISSION, (sender, context) -> {
            ChatUtils.sendMessage(sender, ChatUtils.colorize(prefix + lang.getString("no-permission")));
            if (sender instanceof Player player) SoundUtils.sendSound(player, "invalid-command");
        });
    }
}
