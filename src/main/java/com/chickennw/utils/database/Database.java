package com.chickennw.utils.database;

import jakarta.persistence.Entity;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.reflections.Reflections;

import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Getter
public class Database {

    protected final ExecutorService executor;
    protected SessionFactory sessionFactory;

    public Database(JavaPlugin plugin) {
        FileConfiguration config = plugin.getConfig();
        int threadCount = config.getInt("database.threads");

        if (config.getBoolean("enable-virtual-threads")) {
            ThreadFactory factory = Thread.ofVirtual()
                    .name(plugin.getClass().getSimpleName() + "-database-worker-", 0)
                    .uncaughtExceptionHandler((thread, throwable) -> throwable.printStackTrace())
                    .factory();
            executor = Executors.newThreadPerTaskExecutor(factory);
        } else {
            executor = Executors.newFixedThreadPool(threadCount, r -> {
                Thread t = new Thread(r);
                t.setName(plugin.getClass().getSimpleName() + "-database-worker-" + t.threadId());
                t.setUncaughtExceptionHandler((thread, throwable) -> throwable.printStackTrace());
                return t;
            });
        }

        try {
            Thread.currentThread().setContextClassLoader(plugin.getClass().getClassLoader());

            Configuration configuration = new Configuration();
            Properties settings = new Properties();
            String type = config.getString("database.type");

            settings.put("hibernate.hbm2ddl.auto", "update");
            settings.put("hibernate.show_sql", "false");
            settings.put("log4j.logger.org.hibernate", "DEBUG");
            settings.put("log4j.logger.org.hibernate.SQL", "DEBUG");
            settings.put("hibernate.connection.provider_class", "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");

            String minIdle = config.getString("database.min-idle");
            String maxPool = config.getString("database.max-pool");
            String idleTimeout = config.getString("database.idle-timeout");

            settings.put("hibernate.hikari.minimumIdle", minIdle);
            settings.put("hibernate.hikari.maximumPoolSize", maxPool);
            settings.put("hibernate.hikari.idleTimeout", idleTimeout);
            settings.put("hibernate.hikari.poolName", plugin.getClass().getSimpleName() + "DatabasePool");

            if (type.equalsIgnoreCase("mysql")) {
                String host = config.getString("database.host");
                String port = config.getString("database.port");
                String database = config.getString("database.database");
                String user = config.getString("database.user");
                String password = config.getString("database.password");
                String url = String.format("jdbc:mariadb://%s:%s/%s", host, port, database);

                //settings.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
                settings.put("hibernate.connection.driver_class", "org.mariadb.jdbc.Driver");
                settings.put("hibernate.connection.url", url);
                settings.put("hibernate.connection.username", user);
                settings.put("hibernate.connection.password", password);
            } else {
                String path = plugin.getDataFolder().getAbsolutePath();
                settings.put("hibernate.connection.driver_class", "org.h2.Driver");
                settings.put("hibernate.connection.url", "jdbc:h2:" + path + "/database;AUTO_RECONNECT=TRUE;FILE_LOCK=NO");
            }

            configuration.setProperties(settings);

            Reflections reflections = new Reflections(plugin.getClass() + ".models");
            Set<Class<?>> entityClasses = reflections.getTypesAnnotatedWith(Entity.class);
            for (Class<?> entityClass : entityClasses) {
                configuration.addAnnotatedClass(entityClass);
            }

            StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());
            sessionFactory = configuration.buildSessionFactory(builder.build());
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    public CompletableFuture<Void> save(Object object) {
        return CompletableFuture.runAsync(() -> {
            saveSync(object);
        }, executor);
    }

    public void saveSync(Object object) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.saveOrUpdate(object);
            session.flush();
            session.evict(object);
            tx.commit();
        } catch (Exception ex) {
            throw new RuntimeException("An error appeared on saving spawners sync", ex);
        }
    }

    public CompletableFuture<Void> delete(Object object) {
        return CompletableFuture.runAsync(() -> {
            try (Session session = sessionFactory.openSession()) {
                Transaction tx = session.beginTransaction();

                session.remove(object);
                tx.commit();
            }
        }, executor);
    }

    public void close() {
        executor.shutdown();
        if (sessionFactory != null) sessionFactory.close();
    }
}