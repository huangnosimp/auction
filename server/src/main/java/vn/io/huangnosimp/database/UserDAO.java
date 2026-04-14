package vn.io.huangnosimp.database;



import java.sql.*;

public class UserDAO {
    public boolean checkUsername(String username, int so) {
        String sql = "SELECT username FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (Exception e) {
            System.err.println("Lỗi DB khi check username: " + e.getMessage());
            return false;
        }
    }
    public boolean saveUser(String id, String username, String password, String email, String role) {
        String sql = "INSERT INTO Users (id, username, password, email, role) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
            PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, id);
            stmt.setString(2, username);
            stmt.setString(3, password);
            stmt.setString(4, email);
            stmt.setString(5, role);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi DB khi lưu user: " + e.getMessage());
            return false;
        }
    }
}
