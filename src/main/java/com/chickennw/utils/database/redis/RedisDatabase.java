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
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
public abstract class RedisDatabase {

    private static final long INITIAL_SUBSCRIBE_DEBOUNCE_MS = 150L;
    private static final long EMPTY_CHANNEL_POLL_MS = 50L;
    private static final long RECONNECT_BACKOFF_MS = 3000L;

    private final Set<String> subscribedChannels = ConcurrentHashMap.newKeySet();
    private final AtomicBoolean subscriberRunning = new AtomicBoolean(false);
    protected final ExecutorService subscriberExecutor;
    protected final ExecutorService publisherMessageExecutor;
    protected final ExecutorService subscriberMessageExecutor;
    protected final RedisClient redisClient;
    protected final Logger logger;
    protected final RedisClient subscriberClient;
    protected volatile JedisPubSub jedisPubSub;

    public RedisDatabase(RedisConfiguration redisConfiguration) {
        subscriberExecutor = Executors.newSingleThreadExecutor(r -> {
            String name = ChickenUtils.getPlugin().getName();
            return new Thread(r, name + "-PubSub");
        });
        publisherMessageExecutor = Executors.newSingleThreadExecutor(r -> {
            String name = ChickenUtils.getPlugin().getName();
            return new Thread(r, name + "-PubSub");
        });
        subscriberMessageExecutor = Executors.newSingleThreadExecutor(r -> {
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
        subscriberExecutor = Executors.newSingleThreadExecutor(r -> {
            String name = ChickenUtils.getPlugin().getName();
            return new Thread(r, name + "-PubSub");
        });
        publisherMessageExecutor = Executors.newSingleThreadExecutor(r -> {
            String name = ChickenUtils.getPlugin().getName();
            return new Thread(r, name + "-PubSub");
        });
        subscriberMessageExecutor = Executors.newSingleThreadExecutor(r -> {
            String name = ChickenUtils.getPlugin().getName();
            return new Thread(r, name + "-PubSub");
        });
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
        publisherMessageExecutor.submit(() -> {
            redisClient.publish(message.channel(), message.payload());
        });
    }

    public void subscribe(String channel) {
        boolean newChannel = subscribedChannels.add(channel);
        if (subscriberRunning.compareAndSet(false, true)) {
            subscriberExecutor.submit(this::runSubscriberLoop);
        } else if (newChannel) {
            JedisPubSub active = jedisPubSub;
            if (active != null && active.isSubscribed()) {
                try {
                    active.subscribe(channel);
                } catch (Exception ex) {
                    logger.error("Failed to add channel {} to active subscription: {}", channel, ex.getMessage(), ex);
                }
            }
        }

        logger.info("Subscribed to channel: {}", channel);
    }

    public void unsubscribe() {
        JedisPubSub active = jedisPubSub;
        if (active != null && active.isSubscribed()) {
            active.unsubscribe();
        }

        subscribedChannels.clear();
    }

    private void runSubscriberLoop() {
        try {
            Thread.sleep(INITIAL_SUBSCRIBE_DEBOUNCE_MS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            subscriberRunning.set(false);
            return;
        }
        while (!Thread.currentThread().isInterrupted()) {
            try {
                while (subscribedChannels.isEmpty()) {
                    Thread.sleep(EMPTY_CHANNEL_POLL_MS);
                }
                String[] channels = subscribedChannels.toArray(new String[0]);
                jedisPubSub = new JedisPubSub() {
                    @Override
                    public void onMessage(String channelName, String message) {
                        subscriberMessageExecutor.submit(() -> {
                            try {
                                if (!subscribedChannels.contains(channelName)) return;

                                RedisDatabase.this.onMessage(channelName, message);
                            } catch (Exception ex) {
                                logger.error("Error on message: {}", ex.getMessage(), ex);
                            }
                        });
                    }
                };
                logger.info("Subscribing to Redis channels ({}): {}", channels.length, String.join(", ", channels));
                subscriberClient.subscribe(jedisPubSub, channels);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.error("Redis subscribe crashed, retrying in 3s", e);
                try {
                    Thread.sleep(RECONNECT_BACKOFF_MS);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        subscriberRunning.set(false);
    }

    public void close() {
        unsubscribe();
        subscriberExecutor.shutdownNow();
        redisClient.close();
        subscriberClient.close();
    }

    public abstract void onMessage(String channel, String message);
}