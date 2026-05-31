package vn.io.huangnosimp.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuctionParticipantsRepositoryTest extends H2RepositoryTestSupport {
    private AuctionParticipantsRepository repository;

    @BeforeEach
    void setUpRepository() {
        repository = new AuctionParticipantsRepository(databaseConnection);
    }

    @Test
    void addParticipantIsIdempotentAndCanBeRemoved() throws SQLException {
        repository.addParticipant("auction-1", "user-1");
        repository.addParticipant("auction-1", "user-1");

        assertTrue(repository.isParticipant("auction-1", "user-1"));
        assertEquals(1, countRows("auctionparticipants"));

        repository.removeParticipant("auction-1", "user-1");

        assertFalse(repository.isParticipant("auction-1", "user-1"));
        assertEquals(0, countRows("auctionparticipants"));
    }
}
