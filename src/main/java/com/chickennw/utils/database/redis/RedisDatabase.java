package com.chickennw.utils.database.redis;

import com.chickennw.utils.ChickenUtils;
import com.chickennw.utils.models.config.redis.RedisConfiguration;
import com.chickennw.utils.models.redis.RedisMessage;
import lombok.Getter;
import redis.clients.jedis.ConnectionPoolConfig;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.RedisClient;
import redis.clients.jedis.builders.StandaloneClientBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class RedisDatabase {

    private final List<String> subscribedChannels = new ArrayList<>();
    protected final RedisClient redisClient;
    private JedisPubSub jedisPubSub;
    private Thread subscribeThread;

    public RedisDatabase(RedisConfiguration redisConfiguration) {
        this.redisClient = buildClient(
            redisConfiguration.getHost(),
            redisConfiguration.getPort(),
            redisConfiguration.getUser(),
            redisConfiguration.getPassword()
        );
    }

    public RedisDatabase(String host, int port, String password, String user) {
        this.redisClient = buildClient(host, port, user, password);
    }

    private static RedisClient buildClient(String host, int port, String user, String password) {
        StandaloneClientBuilder<RedisClient> builder = RedisClient.builder().hostAndPort(host, port);

        if (password != null && !password.isEmpty()) {
            DefaultJedisClientConfig clientConfig = DefaultJedisClientConfig.builder()
                .user(user)
                .password(password)
                .database(0)
                .connectionTimeoutMillis(2000)
                .socketTimeoutMillis(2000)
                .build();
            builder.clientConfig(clientConfig);
        }

        ConnectionPoolConfig poolConfig = new ConnectionPoolConfig();
        builder.poolConfig(poolConfig);

        return builder.build();
    }

    public void publish(RedisMessage message) {
        redisClient.publish(message.channel(), message.message().toString());
    }

    public void subscribe(String channel) {
        subscribedChannels.add(channel);
        restartSubscription();
    }

    public void unsubscribe() {
        if (jedisPubSub != null && jedisPubSub.isSubscribed()) {
            jedisPubSub.unsubscribe();
        }

        subscribedChannels.clear();
    }

    private void restartSubscription() {
        if (jedisPubSub != null && jedisPubSub.isSubscribed()) {
            jedisPubSub.unsubscribe();
        }

        if (subscribedChannels.isEmpty()) return;

        jedisPubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channelName, String message) {
                if (!subscribedChannels.contains(channelName)) return;
                RedisDatabase.this.onMessage(channelName, message);
            }
        };

        String[] channels = subscribedChannels.toArray(new String[0]);
        String pluginName = ChickenUtils.getPlugin().getName();
        subscribeThread = new Thread(() -> redisClient.subscribe(jedisPubSub, channels), pluginName + "-PubSub");
        subscribeThread.setDaemon(true);
        subscribeThread.start();
    }

    public void close() {
        unsubscribe();
        redisClient.close();
    }

    public abstract void onMessage(String channel, String message);
}