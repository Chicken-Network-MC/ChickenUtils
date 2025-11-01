package com.chickennw.utils.database.redis;

import com.chickennw.utils.models.config.redis.RedisConfiguration;
import com.chickennw.utils.models.redis.RedisMessage;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class RedisDatabase {

    private final List<String> subscribedChannels = new ArrayList<>();

    protected final RedisClient redisClient;
    protected final StatefulRedisConnection<String, String> redisConnection;
    protected final StatefulRedisPubSubConnection<String, String> pubSubConnection;

    public RedisDatabase(RedisConfiguration redisConfiguration) {
        String host = redisConfiguration.getHost();
        int port = redisConfiguration.getPort();
        String password = redisConfiguration.getPassword();
        String user = redisConfiguration.getUser();

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
