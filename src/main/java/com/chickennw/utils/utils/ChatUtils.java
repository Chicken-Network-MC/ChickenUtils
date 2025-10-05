package com.chickennw.utils.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

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

    public static void sendPlayerMessage(Player player, TextComponent component) {
        player.sendMessage(component);
    }

    public static void broadcastMessage(TextComponent component) {
        Bukkit.getServer().sendMessage(component);
    }

    public static void sendSenderMessage(CommandSender sender, TextComponent component) {
        sender.sendMessage(component);
    }

    public static void sendPlayerMessage(Player player, String message) {
        TextComponent component = ChatUtils.colorize(message);
        player.sendMessage(component);
    }

    public static void sendSenderMessage(CommandSender sender, String message) {
        TextComponent component = ChatUtils.colorize(message);
        sender.sendMessage(component);
    }

    public static List<TextComponent> colorize(List<String> message, TagResolver... placeholders) {
        return message.stream().map(m -> colorize(m, placeholders)).toList();
    }

    public static TextComponent colorize(String message, TagResolver... placeholders) {
        if (message == null) return Component.text("null text");

        return (TextComponent) MINI_MESSAGE.deserialize(message, placeholders);
    }

    public static String convert(TextComponent component) {
        return LEGACY_COMPONENT_SERIALIZER.serialize(component);
    }

    public static String colorizeLegacy(String message) {
        if (message == null) return "null text";

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
