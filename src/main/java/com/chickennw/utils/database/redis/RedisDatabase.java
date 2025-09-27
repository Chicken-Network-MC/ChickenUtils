package com.chickennw.utils.database.redis;

import com.chickennw.utils.ChickenUtils;
import com.chickennw.utils.models.redis.RedisMessage;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class RedisDatabase {

    private final RedisPubSubAsyncCommands<String, String> asyncRedisConnection;
    private final List<String> subscribedChannels = new ArrayList<>();

    private RedisDatabase() {
        FileConfiguration yaml = ChickenUtils.getPlugin().getConfig();
        String host = yaml.getString("Redis.host");
        int port = yaml.getInt("Redis.port");
        String password = yaml.getString("Redis.password");
        String user = yaml.getString("Redis.user");

        RedisClient redisClient = RedisClient.create(RedisURI.Builder.redis(host, port).withAuthentication(user, password).withDatabase(1).build());
        asyncRedisConnection = redisClient.connectPubSub().async();

        addListener();
    }

    public void publish(RedisMessage message) {
        asyncRedisConnection.publish(message.channel(), message.message().toString());
    }

    public void subscribe(String channel) {
        asyncRedisConnection.subscribe(channel);
        subscribedChannels.add(channel);
    }

    public void unsubscribe() {
        subscribedChannels.forEach(asyncRedisConnection::unsubscribe);
        subscribedChannels.clear();
    }

    private void addListener() {
        asyncRedisConnection.getStatefulConnection().addListener(new RedisPubSubAdapter<>() {
            @Override
            public void message(String channelName, String message) {
                if (!subscribedChannels.contains(channelName)) return;

                JSONObject jsonObject = new JSONObject(message);
                RedisMessage redisMessage = new RedisMessage(channelName, jsonObject);
                onMessage(redisMessage);
            }
        });
    }

    public abstract void onMessage(RedisMessage redisMessage);
}
