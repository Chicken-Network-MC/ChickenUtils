package com.chickennw.utils.logger;

import com.chickennw.utils.ChickenUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class SpigotLogger implements Logger {

    private final String name;

    public SpigotLogger() {
        Plugin plugin = ChickenUtils.getPlugin();
        name = plugin != null ? plugin.getName() : "ChickenUtils";
    }

    @Override
    public void error(String message, Throwable throwable) {
        error(message);
        throwable.printStackTrace();
    }

    @Override
    public void error(Throwable throwable) {
        error(throwable.getMessage(), throwable);
    }

    @Override
    public void error(String message) {
        Bukkit.getConsoleSender().sendMessage("§7[§b" + name + "§7] §4[ERROR] §c" + message);
    }

    @Override
    public void debug(String message) {
        Bukkit.getConsoleSender().sendMessage("§7[§b" + name + "§7] §e[DEBUG] §f" + message);
    }

    @Override
    public void warn(String message) {
        Bukkit.getConsoleSender().sendMessage("§7[§b" + name + "§7] §e[WARN] §e" + message);
    }

    @Override
    public void info(String message) {
        Bukkit.getConsoleSender().sendMessage("§7[§b" + name + "§7] §e[INFO] §7" + message);
    }
}
