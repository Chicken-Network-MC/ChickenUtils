package com.chickennw.utils.models.config.redis;

import eu.okaeri.configs.OkaeriConfig;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RedisConfiguration extends OkaeriConfig {

    private String host = "localhost";
    private String user = "default";
    private int port = 6379;
    private String password = "";
    private int database = 0;
    private String channel = "exampleChannel";
}
