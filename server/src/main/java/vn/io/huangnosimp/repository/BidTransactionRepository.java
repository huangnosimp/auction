package vn.io.huangnosimp.repository;

import vn.io.huangnosimp.connection.DatabaseConnection;
import vn.io.huangnosimp.model.BidTransaction;

import java.sql.*;

public class BidTransactionRepository implements IBidTransactionRepository {
    DatabaseConnection databaseConnection;

    public BidTransactionRepository(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    @Override
    public void saveBidTransaction(BidTransaction bidTransaction) {
        String sql = "INSERT INTO bidtransactions (id, bidder_id, auction_id, bid_amount, bid_time) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, bidTransaction.getId());
            stmt.setString(2, bidTransaction.getBidderId());
            stmt.setString(3, bidTransaction.getAuctionId());
            stmt.setDouble(4, bidTransaction.getAmount());
            stmt.setTimestamp(5, new Timestamp(bidTransaction.getCreatedAt()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("DB error when saving bid transaction: " + e.getMessage());
        }
    }
}
