package vn.io.huangnosimp.repository;

import vn.io.huangnosimp.connection.DatabaseConnection;
import vn.io.huangnosimp.model.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionRepository implements ITransactionRepository {
    private static final Logger logger = LoggerFactory.getLogger(TransactionRepository.class);
    private final DatabaseConnection databaseConnection;

    public TransactionRepository(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    @Override
    public boolean saveTransaction(Transaction transaction) {
        String sql = "INSERT INTO Transactions (id, user_id, amount, transaction_time, transaction_type) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, transaction.getId());
            stmt.setString(2, transaction.getUserId());
            stmt.setDouble(3, transaction.getAmount());
            stmt.setTimestamp(4, new Timestamp(transaction.getCreatedAt()));
            stmt.setString(5, transaction.getTransactionType().name());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error(
                    "DB error when saving transaction transactionId={} userId={} type={}",
                    transaction.getId(), transaction.getUserId(), transaction.getTransactionType(), e
            );
            return false;
        }
    }
}
