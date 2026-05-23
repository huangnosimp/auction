package vn.io.huangnosimp.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import vn.io.huangnosimp.connection.DatabaseConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

abstract class H2RepositoryTestSupport {
    protected DatabaseConnection databaseConnection;
    private String jdbcUrl;
    private Connection keepAliveConnection;

    @BeforeEach
    void setUpDatabase() throws SQLException {
        jdbcUrl = "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
        keepAliveConnection = DriverManager.getConnection(jdbcUrl);
        databaseConnection = mock(DatabaseConnection.class);
        when(databaseConnection.getConnection()).thenAnswer(invocation -> DriverManager.getConnection(jdbcUrl));
        createSchema();
    }

    @AfterEach
    void tearDownDatabase() throws SQLException {
        try (Statement statement = keepAliveConnection.createStatement()) {
            statement.execute("DROP ALL OBJECTS");
        } finally {
            keepAliveConnection.close();
        }
    }

    protected void execute(String sql) throws SQLException {
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    protected int countRows(String tableName) throws SQLException {
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    protected double queryDouble(String sql) throws SQLException {
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            return resultSet.getDouble(1);
        }
    }

    protected String queryString(String sql) throws SQLException {
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            return resultSet.getString(1);
        }
    }

    private void createSchema() throws SQLException {
        execute("""
                CREATE TABLE Users (
                    id VARCHAR(36) PRIMARY KEY,
                    username VARCHAR(100) NOT NULL UNIQUE,
                    password VARCHAR(255) NOT NULL,
                    email VARCHAR(255) NOT NULL UNIQUE,
                    role VARCHAR(20) NOT NULL,
                    account_balance DOUBLE DEFAULT 0,
                    frozen_balance DOUBLE DEFAULT 0,
                    is_banned BOOLEAN DEFAULT FALSE,
                    ban_until TIMESTAMP NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """);
        execute("""
                CREATE TABLE Items (
                    id VARCHAR(36) PRIMARY KEY,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    owner_id VARCHAR(36) NOT NULL,
                    name VARCHAR(255) NOT NULL,
                    description TEXT,
                    item_type VARCHAR(50) NOT NULL,
                    artist VARCHAR(255),
                    creation_year INT,
                    brand VARCHAR(255),
                    warranty_period INT,
                    engine_type VARCHAR(255),
                    mileage INT,
                    conditions VARCHAR(50) NOT NULL
                )
                """);
        execute("""
                CREATE TABLE ItemImages (
                    item_id VARCHAR(36) NOT NULL,
                    image_url TEXT NOT NULL
                )
                """);
        execute("""
                CREATE TABLE Auctions (
                    id VARCHAR(36) PRIMARY KEY,
                    item_id VARCHAR(36) NOT NULL,
                    seller_id VARCHAR(36) NOT NULL,
                    winner_id VARCHAR(36),
                    start_time TIMESTAMP NOT NULL,
                    end_time TIMESTAMP NOT NULL,
                    starting_price DOUBLE NOT NULL,
                    final_price DOUBLE DEFAULT 0,
                    status VARCHAR(30) NOT NULL,
                    minimum_increment DOUBLE DEFAULT 0,
                    buy_now_price DOUBLE DEFAULT 0,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """);
        execute("""
                CREATE TABLE AuctionParticipants (
                    auction_id VARCHAR(36) NOT NULL,
                    user_id VARCHAR(36) NOT NULL,
                    PRIMARY KEY (auction_id, user_id)
                )
                """);
        execute("""
                CREATE TABLE BidTransactions (
                    id VARCHAR(36) PRIMARY KEY,
                    bidder_id VARCHAR(36) NOT NULL,
                    auction_id VARCHAR(36) NOT NULL,
                    bid_amount DOUBLE NOT NULL,
                    bid_time TIMESTAMP NOT NULL
                )
                """);
        execute("""
                CREATE TABLE Transactions (
                    id VARCHAR(36) PRIMARY KEY,
                    user_id VARCHAR(36) NOT NULL,
                    amount DOUBLE NOT NULL,
                    transaction_time TIMESTAMP NOT NULL,
                    transaction_type VARCHAR(30) NOT NULL
                )
                """);
    }
}
