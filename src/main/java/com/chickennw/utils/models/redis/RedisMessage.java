package com.chickennw.utils.models.redis;

import org.json.JSONObject;

public record RedisMessage(String channel, JSONObject message) {
}
