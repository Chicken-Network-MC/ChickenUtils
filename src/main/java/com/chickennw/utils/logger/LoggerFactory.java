package com.chickennw.utils.logger;

import com.chickennw.utils.ChickenUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

@Slf4j
public class LoggerFactory {

    private static Logger logger;

    public static Logger getLogger() {
        if (logger != null) return logger;

        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            logger = ChickenUtils.getPlugin().getSLF4JLogger();
        } catch (ClassNotFoundException e) {
            logger = log;
        }

        return logger;
    }
}
