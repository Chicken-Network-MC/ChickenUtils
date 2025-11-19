package com.chickennw.utils.logger;

import com.chickennw.utils.ChickenUtils;
import org.bukkit.plugin.Plugin;
import org.slf4j.LoggerFactory;

public class PaperLogger implements Logger {

    private final org.slf4j.Logger logger;

    public PaperLogger() {
        Plugin plugin = ChickenUtils.getPlugin();
        logger = plugin == null ? LoggerFactory.getLogger(Logger.class) : plugin.getSLF4JLogger();
    }

    @Override
    public void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    @Override
    public void error(Throwable throwable) {
        logger.error(throwable.getMessage(), throwable);
    }

    @Override
    public void error(String message) {
        logger.error(message);
    }

    @Override
    public void debug(String message) {
        logger.debug(message);
    }

    @Override
    public void warn(String message) {
        logger.warn(message);
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }
}
