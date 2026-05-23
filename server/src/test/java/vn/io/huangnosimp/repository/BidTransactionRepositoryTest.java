package vn.io.huangnosimp.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.io.huangnosimp.model.BidTransaction;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BidTransactionRepositoryTest extends H2RepositoryTestSupport {
    private BidTransactionRepository repository;

    @BeforeEach
    void setUpRepository() {
        repository = new BidTransactionRepository(databaseConnection);
    }

    @Test
    void saveBidTransactionPersistsAllFields() throws SQLException {
        BidTransaction bidTransaction = new BidTransaction("bid-1", "auction-1", "bidder-1", 250.0, 1_700_000_000_000L);

        repository.saveBidTransaction(bidTransaction);

        assertEquals(1, countRows("BidTransactions"));
        assertEquals("bidder-1", queryString("SELECT bidder_id FROM BidTransactions WHERE id = 'bid-1'"));
        assertEquals("auction-1", queryString("SELECT auction_id FROM BidTransactions WHERE id = 'bid-1'"));
        assertEquals(250.0, queryDouble("SELECT bid_amount FROM BidTransactions WHERE id = 'bid-1'"));
    }
}
