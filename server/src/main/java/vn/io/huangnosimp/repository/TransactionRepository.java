package vn.io.huangnosimp.repository;

import vn.io.huangnosimp.database.DatabaseConnection;
import vn.io.huangnosimp.model.BidTransaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class TransactionRepository implements ITransactionRepository {
    private final DatabaseConnection databaseConnection;

    public TransactionRepository(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    @Override
    public void saveTransaction(BidTransaction transaction, String sellerId) {
        String sql = "INSERT INTO Transactions (id, auction_id, buyer_id, seller_id, amount, transaction_time) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, transaction.getId());
            stmt.setString(2, transaction.getAuctionId());
            stmt.setString(3, transaction.getBidderId());
            stmt.setString(4, sellerId);
            stmt.setDouble(5, transaction.getAmount());
            stmt.setTimestamp(6, new Timestamp(transaction.getCreatedAt()));

            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("DB error when saving transaction: " + e.getMessage());
        }
    }
}
