package com.chickennw.utils.database.redis;

import com.chickennw.utils.ChickenUtils;
import com.chickennw.utils.models.redis.RedisMessage;
import com.google.gson.Gson;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class RedisDatabase {

    private final List<String> subscribedChannels = new ArrayList<>();

    private final RedisClient redisClient;
    private final StatefulRedisConnection<String, String> redisConnection;
    private final StatefulRedisPubSubConnection<String, String> pubSubConnection;

    private final Gson gson;

    public RedisDatabase() {
        this(new Gson());
    }

    public RedisDatabase(Gson gson) {
        this(ChickenUtils.getPlugin().getConfig().getConfigurationSection("Redis"), gson);
    }

    public RedisDatabase(ConfigurationSection yaml) {
        this(yaml, new Gson());
    }

    public RedisDatabase(ConfigurationSection yaml, Gson gson) {
        this.gson = gson;

        String host = yaml.getString("host");
        int port = yaml.getInt("port");
        String password = yaml.getString("password");
        String user = yaml.getString("user");

        redisClient = RedisClient.create(RedisURI.Builder.redis(host, port).withAuthentication(user, password).withDatabase(0).build());
        pubSubConnection = redisClient.connectPubSub();
        redisConnection = redisClient.connect();

        addListener();
    }

    public void publish(RedisMessage message) {
        redisConnection.async().publish(message.channel(), message.message().toString());
    }

    public void subscribe(String channel) {
        pubSubConnection.async().subscribe(channel);
        subscribedChannels.add(channel);
    }

    public void unsubscribe() {
        subscribedChannels.forEach(pubSubConnection.sync()::unsubscribe);
        subscribedChannels.clear();
    }

    private void addListener() {
        pubSubConnection.async().getStatefulConnection().addListener(new RedisPubSubAdapter<>() {
            @Override
            public void message(String channelName, String message) {
                if (!subscribedChannels.contains(channelName)) return;

                onMessage(channelName, message);
            }
        });
    }

    public void close() {
        unsubscribe();
        redisClient.close();
    }

    public abstract void onMessage(String channel, String message);

}
