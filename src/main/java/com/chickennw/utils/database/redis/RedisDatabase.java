package com.chickennw.utils.database.redis;

import com.chickennw.utils.ChickenUtils;
import com.chickennw.utils.cross.CrossTeleportManager;
import com.chickennw.utils.cross.PendingCrossLocationTeleport;
import com.chickennw.utils.cross.PendingCrossPlayerTeleport;
import com.chickennw.utils.logger.LoggerFactory;
import com.chickennw.utils.models.config.redis.RedisConfiguration;
import com.chickennw.utils.models.redis.RedisMessage;
import lombok.Getter;
import org.json.JSONObject;
import org.slf4j.Logger;
import redis.clients.jedis.*;
import redis.clients.jedis.builders.StandaloneClientBuilder;

import java.util.Set;
import java.util.UUID;
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
    protected final String server;
    protected final String channel;
    protected final String serverGroup;
    protected final ExecutorService subscriberExecutor;
    protected final ExecutorService publisherMessageExecutor;
    protected final ExecutorService subscriberMessageExecutor;
    protected final RedisClient redisClient;
    protected final Logger logger;
    protected final RedisClient subscriberClient;
    protected final CrossTeleportManager crossTeleportManager;
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

        String redisHost = System.getenv("REDIS_HOST") == null ? redisConfiguration.getHost() : System.getenv("REDIS_HOST");
        int port = System.getenv("REDIS_PORT") == null ? redisConfiguration.getPort() : Integer.parseInt(System.getenv("REDIS_PORT"));
        String redisUser = System.getenv("REDIS_USER") == null ? redisConfiguration.getUser() : System.getenv("REDIS_USER");
        String redisPassword = System.getenv("REDIS_PASSWORD") == null ? redisConfiguration.getPassword() : System.getenv("REDIS_PASSWORD");
        redisClient = buildClient(redisHost, port, redisUser, redisPassword);
        subscriberClient = buildClient(redisHost, port, redisUser, redisPassword);

        ChickenUtils.setRedisDatabase(this);

        server = System.getenv("SERVER_ID") == null ? redisConfiguration.getServer() : System.getenv("SERVER_ID");
        serverGroup = System.getenv("SERVER_GROUP_ID") == null ? redisConfiguration.getServerGroup() : System.getenv("SERVER_GROUP_ID");
        channel = redisConfiguration.getChannel();
        crossTeleportManager = new CrossTeleportManager(server, this, channel, serverGroup);
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

                                JSONObject json = new JSONObject(message);
                                String method = json.getString("method");
                                if (method.equals(CrossTeleportManager.TELEPORT_LOCATION_KEY)) {
                                    UUID uuid = UUID.fromString(json.getString("player"));
                                    String targetServer = json.getString("targetServer");
                                    String serverGroup = json.optString("serverGroup");
                                    boolean matchServer = targetServer.equalsIgnoreCase(crossTeleportManager.getServerName());
                                    boolean matchGroup = !serverGroup.isEmpty() && serverGroup.equalsIgnoreCase(crossTeleportManager.getServerGroup());
                                    if (!matchServer && !matchGroup) return;

                                    String world = json.getString("world");
                                    double x = json.getDouble("x");
                                    double y = json.getDouble("y");
                                    double z = json.getDouble("z");
                                    float yaw = (float) json.getDouble("yaw");
                                    float pitch = (float) json.getDouble("pitch");

                                    PendingCrossLocationTeleport requsest = new PendingCrossLocationTeleport(uuid, targetServer, serverGroup, world, x, y, z,
                                        yaw, pitch, System.currentTimeMillis());

                                    crossTeleportManager.checkTeleportLocationOnlinePlayer(uuid, requsest);
                                    return;
                                } else if (method.equals(CrossTeleportManager.TELEPORT_PLAYER_KEY)) {
                                    UUID uuid = UUID.fromString(json.getString("player"));
                                    String targetServer = json.getString("targetServer");
                                    String serverGroup = json.optString("serverGroup");
                                    boolean matchServer = targetServer.equalsIgnoreCase(crossTeleportManager.getServerName());
                                    boolean matchGroup = !serverGroup.isEmpty() && serverGroup.equalsIgnoreCase(crossTeleportManager.getServerGroup());
                                    if (!matchServer && !matchGroup) return;


                                    UUID target = UUID.fromString(json.getString("target"));

                                    PendingCrossPlayerTeleport requsest = new PendingCrossPlayerTeleport(uuid, target, targetServer, serverGroup, System.currentTimeMillis());
                                    crossTeleportManager.checkTeleportPlayerOnlinePlayer(uuid, requsest);
                                    return;
                                }

                                RedisDatabase.this.onMessage(channelName, message);
                            } catch (Exception ex) {
                                logger.error("Error on message: {} -> {}", message, ex.getMessage(), ex);
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