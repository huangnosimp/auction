package vn.io.huangnosimp.service;

import vn.io.huangnosimp.model.*;

import org.junit.jupiter.api.Test;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.*;

public class AuctionServiceTest {
    @Test
    public void testOpenAuctionDuration() {
        AuctionService auctionService = AuctionService.getInstance();
        Auction auction = auctionService.openAuction("SellerId", "ItemId", 100, 2);
        assertNotNull(auction);
        assertEquals(AuctionStatus.RUNNING, auction.getStatus());
        assertEquals(100, auction.getCurrentPrice());
        assertEquals("SellerId", auction.getSellerId());
        assertEquals("ItemId", auction.getItemId());
        assertNotNull(auctionService.getAuctions().get(auction.getId()));
        await().atMost(2, TimeUnit.MINUTES).untilAsserted(() -> assertEquals(AuctionStatus.FINISHED, auction.getStatus()));
    }
    @Test
    public void testOpenAuctionWithTime() {
        AuctionService auctionService = AuctionService.getInstance();
        long now = System.currentTimeMillis();
        long startTime = now + 2 * 60 * 1000;
        long endTime = now + 3 * 60 * 1000;
        Auction auction = auctionService.openAuction("SellerId", "ItemId", 100, startTime, endTime);
        assertNotNull(auction);
        assertEquals(AuctionStatus.OPEN, auction.getStatus());
        assertEquals(100, auction.getCurrentPrice());
        assertEquals("SellerId", auction.getSellerId());
        assertEquals("ItemId", auction.getItemId());
        assertNotNull(auctionService.getAuctions().get(auction.getId()));
        await().atMost(2, TimeUnit.MINUTES).untilAsserted(() -> assertEquals(AuctionStatus.OPEN, auction.getStatus()));
        await().atMost(3, TimeUnit.MINUTES).untilAsserted(() -> assertEquals(AuctionStatus.FINISHED, auction.getStatus()));
    }
    @Test
    public void testPlaceBid() {
        AuctionService auctionService = AuctionService.getInstance();
        Auction auction = AuctionService.getInstance().openAuction("SellerId", "ItemId", 100, 30);
        boolean result = auctionService.placeBid("BidderId", auction.getId(), 150);
        assertTrue(result);
        assertEquals(150, auction.getCurrentPrice());
        assertEquals("BidderId", auction.getCurrentWinnerId());
        boolean wrongResult = auctionService.placeBid("BidderId2", auction.getId(), 120);
        assertFalse(wrongResult);
        assertEquals(150, auction.getCurrentPrice());
        assertEquals("BidderId", auction.getCurrentWinnerId());
    }
}
