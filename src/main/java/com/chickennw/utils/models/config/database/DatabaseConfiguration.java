package com.chickennw.utils.models.config.database;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DatabaseConfiguration extends OkaeriConfig {

    @Comment("You can use h2 and mysql")
    private String type = "h2";
    private int threads = 5;
    private int saveIntervalInMinutes = 15;
    private boolean enableVirtualThreads = true;
    private String threadNamePrefix = "utils";
    private int batchSaveSize = 500;
    private MySQL mysql = new MySQL();

    @Setter
    @Getter
    public static class MySQL extends OkaeriConfig {

        private String host = "localhost";
        private String port = "3306";
        private String database = "db";
        private String user = "user";
        private String password = "mypassword";
        private String minIdle = "15";
        private String maxPool = "30";
        private String idleTimeout = "30000";
    }
}