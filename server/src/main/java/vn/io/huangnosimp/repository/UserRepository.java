package vn.io.huangnosimp.repository;

import vn.io.huangnosimp.connection.DatabaseConnection;
import vn.io.huangnosimp.model.Admin;
import vn.io.huangnosimp.model.Member;
import vn.io.huangnosimp.model.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserRepository implements IUserRepository {
    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);
    private final DatabaseConnection databaseConnection;

    public UserRepository(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    @Override
    public boolean checkUsername(String Username) {
        String sql = "SELECT 1 FROM Users WHERE username = ? LIMIT 1";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, Username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("DB error when checking username", e);
            return false;
        }
    }

    @Override
    public boolean checkEmail(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ? LIMIT 1";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("DB error when checking email", e);
            return false;
        }
    }

    @Override
    public boolean saveUser(String userId, String username, String password, String email, String role) {
        String sql = "INSERT INTO Users (id, username, password, email, role) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = databaseConnection.getConnection();
            PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, username);
            stmt.setString(3, password);
            stmt.setString(4, email);
            stmt.setString(5, role);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("DB error when saving user userId={} role={}", userId, role, e);
            return false;
        }
    }

    @Override
    public User findByUsername(String username) {
        String sql = "SELECT * FROM Users WHERE username = ?";
        try (Connection connection = databaseConnection.getConnection();
            PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("DB error when finding user by username", e);
        }
        return null;
    }

    @Override
    public User findById(String userId) {
        String sql = "SELECT * FROM Users WHERE id = ?";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("DB error when finding user by id userId={}", userId, e);
        }
        return null;
    }

    @Override
    public boolean updateBalance(String userId, double newBalance) {
        String sql = "UPDATE Users SET account_balance = ? WHERE id = ?";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, newBalance);
            stmt.setString(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("DB error when updating balance userId={}", userId, e);
            return false;
        }
    }

    @Override
    public boolean updateFrozenBalance(String userId, double newFrozenBalance) {
        String sql = "UPDATE Users SET frozen_balance = ? WHERE id = ?";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, newFrozenBalance);
            stmt.setString(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("DB error when updating frozen balance userId={}", userId, e);
            return false;
        }
    }
    private User mapRowToUser(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String username = rs.getString("username");
        String password = rs.getString("password");
        String email = rs.getString("email");
        String role = rs.getString("role");
        double accountBalance = rs.getDouble("account_balance");
        double frozenBalance = rs.getDouble("frozen_balance");
        boolean isBanned = rs.getBoolean("is_banned");

        Timestamp banUntilTs = rs.getTimestamp("ban_until");
        LocalDateTime banUntil = (banUntilTs != null) ? banUntilTs.toLocalDateTime() : null;

        long createdAt = rs.getTimestamp("created_at").getTime();

        return switch (role) {
            case "MEMBER" -> new Member(id, username, password, email, accountBalance, frozenBalance, isBanned, banUntil, createdAt);
            case "ADMIN" -> new Admin(id, username, password, email, createdAt);
            default -> null;
        };
    }

    @Override
    public boolean updateBanStatus(String userId, boolean isBanned, LocalDateTime banUntil) {
        String sql = "UPDATE Users SET is_banned = ?, ban_until = ? WHERE id = ?";

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setBoolean(1, isBanned);
            if (banUntil != null) {
                stmt.setTimestamp(2, Timestamp.valueOf(banUntil));
            } else {
                stmt.setNull(2, java.sql.Types.TIMESTAMP);
            }

            stmt.setString(3, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("DB error updating ban status userId={} isBanned={}", userId, isBanned, e);
            return false;
        }
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM Users";

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                User user = mapRowToUser(rs);
                if (user != null) {
                    users.add(user);
                }
            }
        } catch (SQLException e) {
            logger.error("DB error when finding all users", e);
        }
        return users;
    }
}
