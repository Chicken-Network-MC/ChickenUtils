package com.chickennw.utils.managers;

import com.chickennw.utils.logger.LoggerFactory;
import com.chickennw.utils.models.migrators.AbstractPluginMigrator;
import com.chickennw.utils.models.migrators.NotSupportedMigrator;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MigrateManager {

    private static MigrateManager instance;
    private final ConcurrentHashMap<String, AbstractPluginMigrator> migrators = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger();

    public static MigrateManager getInstance() {
        if (instance == null) {
            instance = new MigrateManager();
        }
        return instance;
    }

    public void registerMigrator(String migratorName, Class<? extends AbstractPluginMigrator> migratorClass) {
        try {
            AbstractPluginMigrator migrator = migratorClass.getDeclaredConstructor().newInstance();
            migrators.put(migratorName, migrator);
        } catch (Exception e) {
            logger.error("Can't register migrator {}", migratorName);

            NotSupportedMigrator notSupportedMigrator = new NotSupportedMigrator(migratorName);
            migrators.put(migratorName, notSupportedMigrator);
        }
    }

    public List<AbstractPluginMigrator> getMigrators() {
        return List.copyOf(migrators.values());
    }

    public AbstractPluginMigrator getMigrator(String migratorName) {
        AbstractPluginMigrator migrator = migrators.values()
                .stream()
                .filter(m -> m.getPluginName().equalsIgnoreCase(migratorName))
                .findFirst()
                .orElse(null);

        if (migrator == null) migrator = new NotSupportedMigrator(migratorName);
        return migrator;
    }
}
