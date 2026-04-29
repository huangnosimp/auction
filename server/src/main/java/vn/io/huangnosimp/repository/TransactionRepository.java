package vn.io.huangnosimp.repository;

import vn.io.huangnosimp.database.DatabaseConnection;
import vn.io.huangnosimp.model.Transaction;

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
    public void saveTransaction(Transaction transaction) {
        String sql = "INSERT INTO Transactions (id, user_id, amount, transaction_time, transaction_type) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, transaction.getId());
            stmt.setString(2, transaction.getUserId());
            stmt.setDouble(3, transaction.getAmount());
            stmt.setTimestamp(6, new Timestamp(transaction.getCreatedAt()));
            stmt.setString(4, transaction.getTransactionType().name());

            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("DB error when saving transaction: " + e.getMessage());
        }
    }
}
