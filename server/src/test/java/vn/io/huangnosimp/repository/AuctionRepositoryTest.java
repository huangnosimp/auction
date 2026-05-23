package vn.io.huangnosimp.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.io.huangnosimp.enums.AuctionStatus;
import vn.io.huangnosimp.model.Art;
import vn.io.huangnosimp.model.Auction;
import vn.io.huangnosimp.model.Member;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static vn.io.huangnosimp.TestFixtures.art;

class AuctionRepositoryTest extends H2RepositoryTestSupport {
    private UserRepository userRepository;
    private ItemRepository itemRepository;
    private AuctionRepository auctionRepository;

    @BeforeEach
    void setUpRepositories() {
        userRepository = new UserRepository(databaseConnection);
        itemRepository = new ItemRepository(databaseConnection);
        auctionRepository = new AuctionRepository(databaseConnection, userRepository, itemRepository);
    }

    @Test
    void saveAndFindAuctionWithPricingAndState() {
        Member seller = saveSeller();
        Art item = art(seller.getId());
        itemRepository.save(item);
        Auction auction = new Auction(
                item,
                seller,
                100.0,
                System.currentTimeMillis() - 1_000,
                System.currentTimeMillis() + 60_000,
                25.0,
                1_000.0
        );
        auction.setId("auction-1");
        auction.setStatus(AuctionStatus.RUNNING);
        auction.setCurrentWinnerId("winner-1");
        auction.setCurrentPrice(250.0);

        auctionRepository.save(auction);
        Auction found = auctionRepository.findById("auction-1");

        assertEquals("auction-1", found.getId());
        assertEquals(item.getId(), found.getItem().getId());
        assertEquals(seller.getId(), found.getSeller().getId());
        assertEquals("winner-1", found.getCurrentWinnerId());
        assertEquals(250.0, found.getCurrentPrice());
        assertEquals(AuctionStatus.RUNNING, found.getStatus());
        assertEquals(25.0, found.getMinimumIncrement());
        assertEquals(1_000.0, found.getBuyNowPrice());
    }

    @Test
    void saveUpdatesExistingAuction() {
        Member seller = saveSeller();
        Art item = art(seller.getId());
        itemRepository.save(item);
        Auction auction = new Auction(item, seller, 100.0, System.currentTimeMillis(), System.currentTimeMillis() + 60_000, 10.0, 500.0);
        auction.setId("auction-1");
        auction.setStatus(AuctionStatus.OPEN);
        auctionRepository.save(auction);

        auction.setStatus(AuctionStatus.RUNNING);
        auction.setCurrentWinnerId("winner-1");
        auction.setCurrentPrice(150.0);
        auctionRepository.save(auction);

        Auction found = auctionRepository.findById("auction-1");
        assertEquals(AuctionStatus.RUNNING, found.getStatus());
        assertEquals("winner-1", found.getCurrentWinnerId());
        assertEquals(150.0, found.getCurrentPrice());
        assertEquals(1, auctionRepository.findAll().size());
    }

    @Test
    void deleteRemovesAuction() {
        Member seller = saveSeller();
        Art item = art(seller.getId());
        itemRepository.save(item);
        Auction auction = new Auction(item, seller, 100.0, System.currentTimeMillis(), System.currentTimeMillis() + 60_000, 10.0, 500.0);
        auction.setId("auction-1");
        auction.setStatus(AuctionStatus.OPEN);
        auctionRepository.save(auction);

        assertTrue(auctionRepository.delete("auction-1"));
        assertNull(auctionRepository.findById("auction-1"));
        assertFalse(auctionRepository.delete("auction-1"));
    }

    private Member saveSeller() {
        userRepository.saveUser("seller-1", "seller", "hash", "seller@example.com", "MEMBER");
        return (Member) userRepository.findById("seller-1");
    }
}
