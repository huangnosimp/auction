package vn.io.huangnosimp.repository;

import vn.io.huangnosimp.connection.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuctionParticipantsRepository implements IAuctionParticipantsRepository {
    DatabaseConnection databaseConnection;

    public AuctionParticipantsRepository(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    @Override
    public void addParticipant(String auctionId, String userId) {
        String sql = "INSERT IGNORE INTO AuctionParticipants (auction_id, user_id) VALUES (?, ?)";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, auctionId);
            stmt.setString(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("DB error adding participant: " + e.getMessage());
        }
    }

    @Override
    public void removeParticipant(String auctionId, String userId) {
        String sql = "DELETE FROM AuctionParticipants WHERE auction_id = ? AND user_id = ?";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, auctionId);
            stmt.setString(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("DB error removing participant: " + e.getMessage());
        }
    }

    @Override
    public boolean isParticipant(String auctionId, String userId) {
        String sql = "SELECT 1 FROM AuctionParticipants WHERE auction_id = ? AND user_id = ? LIMIT 1";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, auctionId);
            stmt.setString(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("DB error checking participant: " + e.getMessage());
            return false;
        }
    }
}
