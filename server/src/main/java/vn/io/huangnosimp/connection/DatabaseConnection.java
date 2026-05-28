package vn.io.huangnosimp.connection;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseConnection {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
    private static final int MAX_THREAD_POOL_SIZE = 20;
    private static final int MIN_IDLE_CONNECTIONS = 5;
    private static final int CONNECTION_TIMEOUT_MS = 30000;
    private static volatile DatabaseConnection instance;
    private final HikariDataSource dataSource;
    private DatabaseConnection() {
        String DB_URL = System.getenv("DB_URL");
        String DB_USERNAME = System.getenv("DB_USERNAME");
        String DB_PASSWORD = System.getenv("DB_PASSWORD");
        if (isBlank(DB_URL) || isBlank(DB_USERNAME) || isBlank(DB_PASSWORD)) {
            logger.error(
                    "Database environment variables are not fully configured dbUrlPresent={} dbUsernamePresent={} dbPasswordPresent={}",
                    !isBlank(DB_URL), !isBlank(DB_USERNAME), !isBlank(DB_PASSWORD));
        }

        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(DB_URL);
        config.setUsername(DB_USERNAME);
        config.setPassword(DB_PASSWORD);

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        config.setMaximumPoolSize(MAX_THREAD_POOL_SIZE);
        config.setMinimumIdle(MIN_IDLE_CONNECTIONS);
        config.setConnectionTimeout(CONNECTION_TIMEOUT_MS);

        this.dataSource = new HikariDataSource(config);
        logger.info(
                "HikariCP connection pool initialized maxPoolSize={} minIdle={} connectionTimeoutMs={}",
                MAX_THREAD_POOL_SIZE, MIN_IDLE_CONNECTIONS, CONNECTION_TIMEOUT_MS);
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
        logger.debug("Acquiring database connection from pool");
        return dataSource.getConnection();
    }
    public void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("HikariCP connection pool closed");
        } else {
            logger.debug("HikariCP connection pool close skipped");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
