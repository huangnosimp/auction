package vn.io.huangnosimp.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.io.huangnosimp.dto.response.AuctionCardDTO;
import vn.io.huangnosimp.dto.response.AuctionDetailResponseDTO;
import vn.io.huangnosimp.enums.ItemType;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StatisticRepositoryTest extends H2RepositoryTestSupport {
    private StatisticRepository repository;

    @BeforeEach
    void setUpRepository() {
        repository = new StatisticRepository(databaseConnection);
    }

    @Test
    void auctionCardsAndDetailsIncludeItemType() throws SQLException {
        execute("""
                INSERT INTO Users (id, username, password, email, role)
                VALUES ('seller-1', 'seller', 'hash', 'seller@example.com', 'MEMBER')
                """);
        execute("""
                INSERT INTO Items (id, owner_id, name, description, item_type, artist, creation_year, conditions)
                VALUES ('item-1', 'seller-1', 'Oil Painting', 'Landscape', 'ART', 'Test Artist', 2024, 'NEW')
                """);
        execute("""
                INSERT INTO ItemImages (item_id, image_url)
                VALUES ('item-1', 'https://example.com/item.png')
                """);
        execute("""
                INSERT INTO Auctions (
                    id, item_id, seller_id, start_time, end_time, starting_price, final_price,
                    status, minimum_increment, buy_now_price
                )
                VALUES (
                    'auction-1', 'item-1', 'seller-1',
                    TIMESTAMP '2026-01-01 00:00:00', TIMESTAMP '2026-01-02 00:00:00',
                    100.0, 0.0, 'OPEN', 10.0, 500.0
                )
                """);

        List<AuctionCardDTO> cards = repository.getPublicAuctionCard(10);
        AuctionDetailResponseDTO detail = repository.getAuctionDetail("auction-1");

        assertEquals(ItemType.ART, cards.get(0).getItemType());
        assertEquals(ItemType.ART, detail.getItemType());
        assertEquals(ItemType.ART, detail.getCategory());
    }
}
