package com.chickennw.utils.database.sql;

import com.chickennw.utils.ChickenUtils;
import com.chickennw.utils.logger.LoggerFactory;
import com.chickennw.utils.models.config.database.DatabaseConfiguration;
import com.chickennw.utils.models.config.head.HeadEntity;
import jakarta.persistence.Entity;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.reflections.Reflections;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.*;

@Getter
public abstract class Database {

    private final String BACKUP_FOLDER = "backup";
    protected final ExecutorService executor;
    protected final Logger logger;
    protected String databaseType;
    protected SessionFactory sessionFactory;

    public Database(JavaPlugin plugin, DatabaseConfiguration config) {
        logger = LoggerFactory.getLogger();
        int threadCount = config.getThreads();

        if (config.isEnableVirtualThreads()) {
            ThreadFactory factory = Thread.ofVirtual()
                    .name(config.getThreadNamePrefix() + "-database-worker-", 0)
                    .uncaughtExceptionHandler((thread, throwable) -> logger.error(throwable.getMessage(), throwable))
                    .factory();
            executor = Executors.newFixedThreadPool(threadCount, factory);
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
        databaseType = config.getType();

        settings.put("hibernate.hbm2ddl.auto", "update");
        settings.put("hibernate.show_sql", "false");
        settings.put("log4j.logger.org.hibernate", "INFO");
        settings.put("log4j.logger.org.hibernate.SQL", "INFO");
        settings.put("hibernate.connection.provider_class", "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");

        String minIdle = databaseType.equalsIgnoreCase("sqlite") ? "1" : config.getMinIdle();
        String maxPool = databaseType.equalsIgnoreCase("sqlite") ? "1" : config.getMaxPool();
        String idleTimeout = config.getIdleTimeout();

        settings.put("hibernate.hikari.idleTimeout", idleTimeout);
        settings.put("hibernate.hikari.poolName", plugin.getClass().getSimpleName() + "DatabasePool");
        settings.put("hibernate.transaction.jta.platform", "org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform");
        settings.put("hibernate.hikari.maxLifetime", "600000");
        settings.put("hibernate.hikari.connectionTimeout", "20000");
        settings.put("hibernate.hikari.leakDetectionThreshold", "60000");
        settings.put("hibernate.hikari.minimumIdle", minIdle);
        settings.put("hibernate.hikari.maximumPoolSize", maxPool);

        if (databaseType.equalsIgnoreCase("mysql")) {
            String host = config.getMysql().getHost();
            String port = config.getMysql().getPort();
            String database = config.getMysql().getDatabase();
            String user = config.getMysql().getUser();
            String password = config.getMysql().getPassword();
            String url = String.format("jdbc:mariadb://%s:%s/%s", host, port, database);

            settings.put("hibernate.connection.driver_class", "org.mariadb.jdbc.Driver");
            settings.put("hibernate.connection.url", url);
            settings.put("hibernate.connection.username", user);
            settings.put("hibernate.connection.password", password);
        } else if (databaseType.equalsIgnoreCase("h2")) {
            String path = plugin.getDataFolder().getAbsolutePath();
            settings.put("hibernate.connection.driver_class", "org.h2.Driver");
            settings.put("hibernate.connection.url", "jdbc:h2:" + path + "/database;" +
                    "AUTO_RECONNECT=TRUE;" +
                    "FILE_LOCK=NO");
        } else if (databaseType.equalsIgnoreCase("hsqldb")) {
            String path = plugin.getDataFolder().getAbsolutePath();
            settings.put("hibernate.connection.driver_class", "org.hsqldb.jdbc.JDBCDriver");
            settings.put("hibernate.connection.url", "jdbc:hsqldb:file:" + path + "/database/database;shutdown=true");
            settings.put("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
            settings.put("hibernate.hikari.connectionTestQuery", "VALUES (1)");
            settings.put("hikari.connection-test-query", "VALUES (1)");
        } else if (databaseType.equalsIgnoreCase("sqlite")) {
            String path = plugin.getDataFolder().getAbsolutePath();
            settings.put("hibernate.connection.driver_class", "org.sqlite.JDBC");
            settings.put("hibernate.connection.url", "jdbc:sqlite:" + path + "/database.db");
            settings.put("hibernate.dialect", "org.hibernate.community.dialect.SQLiteDialect");
        } else {
            throw new IllegalArgumentException("Unsupported database type: " + databaseType);
        }

        return settings;
    }

    public CompletableFuture<Void> save(Object object) {
        return CompletableFuture.runAsync(() -> saveSync(object), executor);
    }

    public void saveSync(Object object) {
        try (StatelessSession session = sessionFactory.openStatelessSession()) {
            Transaction tx = session.beginTransaction();
            try {
                session.upsert(object);
                tx.commit();
            } catch (Exception ex) {
                tx.rollback();
                throw new RuntimeException("An error appeared on saving spawners sync", ex);
            }
        }
    }

    public void deleteSync(Object object) {
        try (StatelessSession session = sessionFactory.openStatelessSession()) {
            Transaction tx = session.beginTransaction();
            try {
                session.delete(object);
                tx.commit();
            } catch (Exception ex) {
                tx.rollback();
                throw new RuntimeException("An error appeared on deleting spawners sync", ex);
            }
        }
    }

    public CompletableFuture<Void> saveList(List<?> objects) {
        return CompletableFuture.runAsync(() -> saveSyncList(objects), executor);
    }

    public void saveSyncList(List<?> objects) {
        try (StatelessSession session = sessionFactory.openStatelessSession()) {
            Transaction tx = session.beginTransaction();

            try {
                for (Object object : objects) {
                    session.upsert(object);
                }

                tx.commit();
            } catch (Exception ex) {
                tx.rollback();
                throw new RuntimeException("An error appeared on saving spawners sync", ex);
            }
        }
    }

    public CompletableFuture<Void> delete(Object object) {
        return CompletableFuture.runAsync(() -> deleteSync(object), executor);
    }

    public void testQuery() {
        try (StatelessSession session = sessionFactory.openStatelessSession()) {
            session.createNativeQuery(
                    databaseType.equalsIgnoreCase("HSQLDB") ? "VALUES (1)" : "SELECT 1"
            ).getSingleResult();
            logger.info("Database connection test successful.");
        } catch (Exception e) {
            logger.error("Database connection test failed.", e);
        }
    }

    public void close() {
        try {
            executor.shutdown();
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                logger.warn("Executor did not terminate in time, forcing shutdown...");
                List<Runnable> droppedTasks = executor.shutdownNow();
                logger.warn("Dropped {} tasks", droppedTasks.size());
            }

            if (sessionFactory != null && !sessionFactory.isClosed()) {
                testQuery();
                takeBackup();
                shutdownH2Gracefully();

                sessionFactory.close();
                logger.info("SessionFactory closed");
            }

            logger.info("Database closed successfully");
        } catch (Exception e) {
            logger.error("Error during database shutdown", e);
        }
    }

    private void takeBackup() {
        File dataFolder = ChickenUtils.getPlugin().getDataFolder();
        File backupDir = new File(dataFolder, BACKUP_FOLDER);
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }

        String fileName = "backup_" + System.currentTimeMillis();
        String backupPath = new File(backupDir, fileName).getAbsolutePath();

        try {
            if (databaseType.equalsIgnoreCase("h2")) backupH2(backupPath);
            else if (databaseType.equalsIgnoreCase("hsqldb")) backupHSQL(backupPath);
            else if (databaseType.equalsIgnoreCase("sqlite")) backupSQLite(backupPath);
        } catch (Exception e) {
            logger.warn("Failed to create backup: {}", e.getMessage());
            logger.debug("Backup error details", e);
        }
    }

    private void backupH2(String backupPath) {
        try (StatelessSession session = sessionFactory.openStatelessSession()) {
            session.doWork(connection -> {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("BACKUP TO '" + backupPath + ".zip'");
                    logger.info("H2 backup completed");
                }
            });
        }
    }

    private void backupHSQL(String backupPath) {
        try (StatelessSession session = sessionFactory.openStatelessSession()) {
            session.doWork(connection -> {
                boolean oldAutoCommit = connection.getAutoCommit();
                try {
                    connection.setAutoCommit(true);

                    try (Statement stmt = connection.createStatement()) {
                        stmt.execute("SCRIPT '" + backupPath + ".sql'");
                        logger.info("HSQLDB backup completed");
                    }
                } finally {
                    connection.setAutoCommit(oldAutoCommit);
                }
            });
        }
    }

    private void backupSQLite(String backupPath) {
        try {
            String dbPath = ChickenUtils.getPlugin().getDataFolder().getAbsolutePath() + "/database.db";
            File sourceFile = new File(dbPath);
            File destFile = new File(backupPath + ".db");

            if (sourceFile.exists()) {
                Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                logger.info("SQLite backup completed");
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to backup SQLite database", ex);
        }
    }

    private void shutdownH2Gracefully() {
        if (!databaseType.equalsIgnoreCase("h2")) return;

        try (StatelessSession session = sessionFactory.openStatelessSession()) {
            session.doWork(connection -> {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("CHECKPOINT SYNC");
                    logger.info("Checkpoint completed");
                }
            });
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("Database is already closed")) {
                logger.info("H2 shutdown compact completed with database already closed.");
            } else {
                logger.warn("Error during H2 shutdown: {}", e.getMessage());
            }
        }
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