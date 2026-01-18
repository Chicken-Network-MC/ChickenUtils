package com.chickennw.utils.utils;

import com.chickennw.utils.ChickenUtils;
import com.chickennw.utils.managers.CommandManager;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

@Slf4j
public class ChatUtils {

    public static final MiniMessage MINI_MESSAGE = MiniMessage.builder()
            .tags(TagResolver.builder().resolver(StandardTags.defaults()).build())
            .strict(false)
            .emitVirtuals(false)
            .postProcessor(component -> component.decoration(TextDecoration.ITALIC, false))
            .build();

    public static final LegacyComponentSerializer LEGACY_COMPONENT_SERIALIZER = LegacyComponentSerializer.builder()
            .character('§')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    public static void broadcastMessage(Component component) {
        Bukkit.getServer().sendMessage(component);
    }

    public static void sendMessage(CommandSender sender, Component component) {
        if (sender instanceof Player player) {
            ChickenUtils.getBukkitAudience().player(player).sendMessage(component);
        } else {
            ChickenUtils.getBukkitAudience().sender(sender).sendMessage(component);
        }
    }

    public static void sendMessage(CommandSender sender, String message) {
        if (sender instanceof Player player) {
            ChickenUtils.getBukkitAudience().player(player).sendMessage(ChatUtils.colorize(message));
        } else {
            sender.sendMessage(ChatUtils.colorizeLegacy(message));
        }
    }

    public static void sendPrefixedMessage(CommandSender sender, String message) {
        FileConfiguration config = CommandManager.getInstance().getConfig();
        String prefix = config.getString("prefix");
        if (sender instanceof Player player) {
            ChickenUtils.getBukkitAudience().player(player).sendMessage(ChatUtils.colorize(prefix + message));
        } else {
            sender.sendMessage(ChatUtils.colorizeLegacy(prefix + message));
        }
    }

    public static List<Component> colorize(List<String> message, TagResolver... placeholders) {
        return message.stream().map(m -> colorize(m, placeholders)).toList();
    }

    public static List<String> colorizeLegacy(List<String> message, Variable... variables) {
        return message.stream().map(s -> colorizeLegacy(s, variables)).toList();
    }

    public static Component colorize(String message, TagResolver... placeholders) {
        if (message == null) return Component.text("null text");

        return MINI_MESSAGE.deserialize(message, placeholders);
    }

    public static String convert(Component component) {
        return LEGACY_COMPONENT_SERIALIZER.serialize(component);
    }

    public static String colorizeLegacy(String message, Variable... variables) {
        if (message == null) return "null text";

        for (Variable variable : variables)
            message = message.replace(variable.key(), variable.value());

        //message = message.replace("&", "§");
        try {
            Component component = MINI_MESSAGE.deserialize(message);
            return LEGACY_COMPONENT_SERIALIZER.serialize(component);
        } catch (Exception e) {
            e.printStackTrace();
            return LEGACY_COMPONENT_SERIALIZER.serialize(LEGACY_COMPONENT_SERIALIZER.deserialize(message));
        }
    }

}
