package vn.io.huangnosimp.database;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DatabaseConnection {
    private static volatile DatabaseConnection instance;
    private final HikariDataSource dataSource;
    private DatabaseConnection() {
        String DB_URL = System.getenv("DB_URL");
        String DB_USERNAME = System.getenv("DB_USERNAME");
        String DB_PASSWORD = System.getenv("DB_PASSWORD");
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(DB_URL);
        config.setUsername(DB_USERNAME);
        config.setPassword(DB_PASSWORD);

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);

        this.dataSource = new HikariDataSource(config);
        System.out.println("[DatabaseConnection] HikariCP Connection Pool initialized successfully.");
    }
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    public void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("[DatabaseConnection] Connection Pool close successfully.");
        }
    }
}
