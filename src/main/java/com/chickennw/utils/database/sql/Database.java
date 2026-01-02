package com.chickennw.utils.database.sql;

import com.chickennw.utils.ChickenUtils;
import com.chickennw.utils.logger.LoggerFactory;
import com.chickennw.utils.models.config.database.DatabaseConfiguration;
import com.chickennw.utils.models.config.head.HeadEntity;
import jakarta.persistence.Entity;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.reflections.Reflections;
import org.slf4j.Logger;

import java.io.File;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Getter
public abstract class Database {

    private final String BACKUP_FOLDER = "backup";
    protected final ExecutorService executor;
    protected final Logger logger;
    protected SessionFactory sessionFactory;

    public Database(JavaPlugin plugin, DatabaseConfiguration config) {
        logger = LoggerFactory.getLogger();
        int threadCount = config.getThreads();

        if (config.isEnableVirtualThreads()) {
            ThreadFactory factory = Thread.ofVirtual()
                    .name(config.getThreadNamePrefix() + "-database-worker-", 0)
                    .uncaughtExceptionHandler((thread, throwable) -> logger.error(throwable.getMessage(), throwable))
                    .factory();
            executor = Executors.newThreadPerTaskExecutor(factory);
        } else {
            executor = Executors.newFixedThreadPool(threadCount, r -> {
                Thread t = new Thread(r);
                t.setName(config.getThreadNamePrefix() + "-database-worker-" + t.threadId());
                t.setUncaughtExceptionHandler((thread, throwable) -> logger.error(throwable.getMessage(), throwable));
                return t;
            });
        }

        try {
            Thread.currentThread().setContextClassLoader(plugin.getClass().getClassLoader());

            Configuration configuration = new Configuration();
            Properties properties = generateProperties(plugin, config);
            configuration.setProperties(properties);

            Reflections reflections = new Reflections(plugin.getClass().getPackage().getName() + ".models");
            Set<Class<?>> entityClasses = reflections.getTypesAnnotatedWith(Entity.class);
            for (Class<?> entityClass : entityClasses) {
                logger.info("Loading entity class: {}", entityClass.getSimpleName());
                configuration.addAnnotatedClass(entityClass);
            }
            configuration.addAnnotatedClass(HeadEntity.class);

            StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());
            sessionFactory = configuration.buildSessionFactory(builder.build());

            deleteOldBackups();
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    private Properties generateProperties(JavaPlugin plugin, DatabaseConfiguration config) {
        Properties settings = new Properties();
        String type = config.getType();

        settings.put("hibernate.hbm2ddl.auto", "update");
        settings.put("hibernate.show_sql", "false");
        settings.put("log4j.logger.org.hibernate", "DEBUG");
        settings.put("log4j.logger.org.hibernate.SQL", "DEBUG");
        settings.put("hibernate.connection.provider_class", "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");

        String minIdle = config.getMysql().getMinIdle();
        String maxPool = config.getMysql().getMaxPool();
        String idleTimeout = config.getMysql().getIdleTimeout();

        settings.put("hibernate.hikari.minimumIdle", minIdle);
        settings.put("hibernate.hikari.maximumPoolSize", maxPool);
        settings.put("hibernate.hikari.idleTimeout", idleTimeout);
        settings.put("hibernate.hikari.poolName", plugin.getClass().getSimpleName() + "DatabasePool");
        settings.put("hibernate.hikari.maxLifetime", "600000");
        settings.put("hibernate.hikari.connectionTimeout", "20000");
        settings.put("hibernate.hikari.leakDetectionThreshold", "60000");
        settings.put("hibernate.hikari.autoCommit", "true");

        if (type.equalsIgnoreCase("mysql")) {
            String host = config.getMysql().getHost();
            String port = config.getMysql().getPort();
            String database = config.getMysql().getDatabase();
            String user = config.getMysql().getUser();
            String password = config.getMysql().getPassword();
            String url = String.format("jdbc:mariadb://%s:%s/%s", host, port, database);

            //settings.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
            settings.put("hibernate.connection.driver_class", "org.mariadb.jdbc.Driver");
            settings.put("hibernate.connection.url", url);
            settings.put("hibernate.connection.username", user);
            settings.put("hibernate.connection.password", password);
        } else {
            String path = plugin.getDataFolder().getAbsolutePath();
            settings.put("hibernate.connection.driver_class", "org.h2.Driver");
            settings.put("hibernate.connection.url", "jdbc:h2:" + path + "/database;" +
                    "DB_CLOSE_ON_EXIT=FALSE;" +
                    "CACHE_SIZE=8192;" +
                    "WRITE_DELAY=1000;" +
                    "FILE_LOCK=FS");
        }

        return settings;
    }

    public CompletableFuture<Void> save(Object object) {
        return CompletableFuture.runAsync(() -> saveSync(object), executor);
    }

    public void saveSync(Object object) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.saveOrUpdate(object);
            session.evict(object);
            tx.commit();
        } catch (Exception ex) {
            throw new RuntimeException("An error appeared on saving spawners sync", ex);
        }
    }

    public CompletableFuture<Void> delete(Object object) {
        return CompletableFuture.runAsync(() -> deleteSync(object), executor);
    }

    public void deleteSync(Object object) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            session.remove(object);
            tx.commit();
        }
    }

    public void backup() {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();

            File dataFolder = ChickenUtils.getPlugin().getDataFolder();
            File backupDir = new File(dataFolder, BACKUP_FOLDER);
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }

            String fileName = "backup_" + System.currentTimeMillis() + ".zip";
            String backupPath = new File(backupDir, fileName).getAbsolutePath();

            String sql = "BACKUP TO '" + backupPath + "'";
            session.createNativeQuery(sql).executeUpdate();
            session.getTransaction().commit();
            logger.info("Backup success.");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void close() {
        executor.shutdown();
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }

        logger.info("Database closed successfully.");
    }

    private void deleteOldBackups() {
        File dataFolder = ChickenUtils.getPlugin().getDataFolder();
        File backupDir = new File(dataFolder, BACKUP_FOLDER);
        if (!backupDir.exists()) {
            return;
        }

        File[] files = backupDir.listFiles();
        if (files == null) {
            return;
        }

        long cutoff = System.currentTimeMillis() - (14L * 24 * 60 * 60 * 1000); // 14 days
        for (File file : files) {
            if (file.isFile() && file.lastModified() < cutoff) {
                if (file.delete()) {
                    logger.info("Deleted old backup file: {}", file.getName());
                } else {
                    logger.warn("Failed to delete old backup file: {}", file.getName());
                }
            }
        }
    }
}