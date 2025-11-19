package com.chickennw.utils.logger;

public class LoggerFactory {

    private static Logger logger;

    public static Logger getLogger() {
        if (logger != null) return logger;

        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            logger = new PaperLogger();
        } catch (ClassNotFoundException e) {
            logger = new SpigotLogger();
        }

        return logger;
    }
}
