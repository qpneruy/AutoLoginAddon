package org.qpneruy.autoLoginAddon.data.Database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import static org.qpneruy.autoLoginAddon.AutoLoginAddon.instance;

public class DatabaseManager {
    @Getter
    private static final HikariDataSource dataSource;
    private static final String DB_NAME = "IpDatabase";

    static {
        dataSource = initializeDataSource();
    }

    private static HikariDataSource initializeDataSource() {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("H2 Driver not found", e);
        }

        HikariConfig config = getHikariConfig();

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        return new HikariDataSource(config);
    }

    private static @NotNull HikariConfig getHikariConfig() {
        String dbPath = instance.getDataFolder() + DB_NAME;
        String dbUrl = "jdbc:h2:" + dbPath + ";AUTO_SERVER=TRUE";

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbUrl);
        config.setPoolName("AuthPool");
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(20000);
        config.setIdleTimeout(300000);
        config.setMaxLifetime(900000);
        return config;
    }

    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}