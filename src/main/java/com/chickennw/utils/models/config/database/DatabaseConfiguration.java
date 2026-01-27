package com.chickennw.utils.models.config.database;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DatabaseConfiguration extends OkaeriConfig {

    @Comment({
            "You can set the database type that you want to use.",
            "Supported Types: ",
            "H2 - Faster in general but has a corruption risk. Using MySQL is much more preferred",
            "MySQL - Recommended in general",
            "HSQLDB - Recommended if you don't want to use MySQL",
            "SQLite - Much safer choice with overall less performance"
    })
    private String type = "sqlite";
    private int threads = 10;
    private int saveIntervalInMinutes = 1;
    private boolean enableVirtualThreads = true;
    private String threadNamePrefix = "utils";
    private int batchSaveSize = 50;
    private String minIdle = "5";
    private String maxPool = "10";
    private String idleTimeout = "30000";

    private MySQL mysql = new MySQL();

    @Setter
    @Getter
    public static class MySQL extends OkaeriConfig {

        private String host = "localhost";
        private String port = "3306";
        private String database = "db";
        private String user = "user";
        private String password = "mypassword";
    }
}