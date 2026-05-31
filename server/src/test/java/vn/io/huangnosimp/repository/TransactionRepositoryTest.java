package vn.io.huangnosimp.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.io.huangnosimp.enums.TransactionType;
import vn.io.huangnosimp.model.Transaction;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransactionRepositoryTest extends H2RepositoryTestSupport {
    private TransactionRepository repository;

    @BeforeEach
    void setUpRepository() {
        repository = new TransactionRepository(databaseConnection);
    }

    @Test
    void saveTransactionPersistsAllFields() throws SQLException {
        Transaction transaction = new Transaction("tx-1", "user-1", TransactionType.DEPOSIT, 300.0, 1_700_000_000_000L);

        repository.saveTransaction(transaction);

        assertEquals(1, countRows("transactions"));
        assertEquals("user-1", queryString("SELECT user_id FROM transactions WHERE id = 'tx-1'"));
        assertEquals(300.0, queryDouble("SELECT amount FROM transactions WHERE id = 'tx-1'"));
        assertEquals("DEPOSIT", queryString("SELECT transaction_type FROM transactions WHERE id = 'tx-1'"));
    }
}
