package vn.io.huangnosimp.service;
import vn.io.huangnosimp.model.Auction;
import vn.io.huangnosimp.model.*;

import java.util.HashMap;
public class AuctionService {
    private static AuctionService insctance;
    private final HashMap<String, Auction> auctions;

    private AuctionService() {
        auctions = new HashMap<>();
    }
    public static synchronized AuctionService getInstance() {
        if (insctance == null) {
            insctance = new AuctionService();
        }
        return insctance;
    }

    public Auction openAuction(String sellerId, String itemId, double startPrice,long startTime, long endTime) {
        Auction auction = new Auction(itemId, sellerId, startPrice, startTime, endTime);
        auctions.put(auction.getId(), auction);
        return auction;
    }
    public Auction openAuction(String sellerId, String itemId, double startPrice, int duration) {
        Auction auction = new Auction(itemId, sellerId, startPrice, duration);
        auctions.put(auction.getId(), auction);
        return auction;
    }
    public boolean placeBid(String bidderId, String auctionId, double amount) {
        Auction auction = auctions.get(auctionId);
        if (auction == null || auction.getStatus() != AuctionStatus.OPEN) {
            return false;
        }
        if (amount <= auction.getCurrentPrice()) {
            return false;
        }
        auction.setCurrentPrice(amount);
        auction.setCurrentWinnerId(bidderId);
        BidTransaction bidTransaction = new BidTransaction(auctionId, bidderId, amount, false);

        return true;
    }
}
