package com.chickennw.utils.logger;

public interface Logger {

    void error(String message, Throwable throwable);

    void error(Throwable throwable);

    void error(String message);

    void debug(String message);

    void warn(String message);

    void info(String message);
}
