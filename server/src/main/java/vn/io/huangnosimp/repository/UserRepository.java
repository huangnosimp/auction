package vn.io.huangnosimp.repository;

import vn.io.huangnosimp.database.DatabaseConnection;
import vn.io.huangnosimp.model.Admin;
import vn.io.huangnosimp.model.Member;
import vn.io.huangnosimp.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRepository implements IUserRepository {
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
            System.err.println("DB error when checking username: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean banUser(String userId) {
        return false;
    }

    @Override
    public boolean unbanUser(String userId) {
        return false;
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
            System.err.println("DB error when checking email: " + e.getMessage());
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
            System.err.println("DB error when saving user: " + e.getMessage());
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
            System.err.println("DB error when finding user by username: " + e.getMessage());
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
            System.err.println("DB error when finding user by ID: " + e.getMessage());
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
            System.err.println("DB error when updating balance: " + e.getMessage());
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
            System.err.println("DB error when updating frozen balance: " + e.getMessage());
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
        long createdAt = rs.getTimestamp("created_at").getTime();

        return switch (role) {
            case "MEMBER" -> new Member(id, username, password, email, accountBalance, frozenBalance, isBanned, createdAt);
            case "ADMIN" -> new Admin(id, username, password, email, createdAt);
            default -> null;
        };
    }
}
