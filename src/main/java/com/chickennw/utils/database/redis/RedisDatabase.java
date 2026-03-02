package com.chickennw.utils.database.redis;

import com.chickennw.utils.ChickenUtils;
import com.chickennw.utils.logger.LoggerFactory;
import com.chickennw.utils.models.config.redis.RedisConfiguration;
import com.chickennw.utils.models.redis.RedisMessage;
import lombok.Getter;
import org.slf4j.Logger;
import redis.clients.jedis.*;
import redis.clients.jedis.builders.StandaloneClientBuilder;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
public abstract class RedisDatabase {

    private final Set<String> subscribedChannels = ConcurrentHashMap.newKeySet();
    protected final ExecutorService subscriberExecutor;
    protected final RedisClient redisClient;
    protected final Logger logger;
    protected final RedisClient subscriberClient;
    protected JedisPubSub jedisPubSub;

    public RedisDatabase(RedisConfiguration redisConfiguration) {
        subscriberExecutor = Executors.newSingleThreadExecutor(r -> {
            String name = ChickenUtils.getPlugin().getName();
            return new Thread(r, name + "-PubSub");
        });
        logger = LoggerFactory.getLogger();

        redisClient = buildClient(
            redisConfiguration.getHost(),
            redisConfiguration.getPort(),
            redisConfiguration.getUser(),
            redisConfiguration.getPassword()
        );
        subscriberClient = buildClient(
            redisConfiguration.getHost(),
            redisConfiguration.getPort(),
            redisConfiguration.getUser(),
            redisConfiguration.getPassword()
        );

        ChickenUtils.setRedisDatabase(this);
    }

    public RedisDatabase(String host, int port, String password, String user) {
        subscriberExecutor = Executors.newSingleThreadExecutor(r -> new Thread(r, ChickenUtils.getPlugin() + "-PubSub"));
        logger = LoggerFactory.getLogger();
        redisClient = buildClient(host, port, user, password);
        subscriberClient = buildClient(host, port, user, password);
        ChickenUtils.setRedisDatabase(this);
    }

    private static RedisClient buildClient(String host, int port, String user, String password) {
        StandaloneClientBuilder<RedisClient> builder = RedisClient.builder().hostAndPort(host, port);

        DefaultJedisClientConfig.Builder configBuilder = DefaultJedisClientConfig.builder()
            .protocol(RedisProtocol.RESP2)
            .database(0)
            .connectionTimeoutMillis(2000)
            .socketTimeoutMillis(2000);

        if (password != null && !password.isEmpty()) {
            configBuilder.user(user).password(password);
        }

        builder.clientConfig(configBuilder.build());

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
        logger.info("Subscribed to channel: {}", channel);
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
                try {
                    if (!subscribedChannels.contains(channelName)) return;

                    RedisDatabase.this.onMessage(channelName, message);
                } catch (Exception ex) {
                    logger.error("Error on message: {}", ex.getMessage(), ex);
                }
            }
        };

        String[] channels = subscribedChannels.toArray(new String[0]);
        subscriberExecutor.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    logger.info("Subscribing to Redis channels...");
                    subscriberClient.subscribe(jedisPubSub, channels);
                } catch (Exception e) {
                    logger.error("Redis subscribe crashed, retrying in 3s", e);
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        });
    }

    public void close() {
        unsubscribe();
        subscriberExecutor.shutdownNow();
        redisClient.close();
        subscriberClient.close();
    }

    public abstract void onMessage(String channel, String message);
}