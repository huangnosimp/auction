package vn.io.huangnosimp.service;

import vn.io.huangnosimp.model.Auction;
import vn.io.huangnosimp.model.BidTransaction;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationService {

    public interface AuctionObserver {
        void onNewBid(Auction auction, BidTransaction bid);

        void onAuctionExtended(Auction auction, long extensionSeconds);

        void onAuctionEnded(Auction auction);
    }

    private final List<AuctionObserver> observers = new CopyOnWriteArrayList<>();

    public void registerObserver(AuctionObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
            System.out.println("[NotificationService] Observer registered: " + observer.getClass().getSimpleName());
        }
    }

    public void removeObserver(AuctionObserver observer) {
        observers.remove(observer);
        System.out.println("[NotificationService] Observer removed: " +
                (observer != null ? observer.getClass().getSimpleName() : "null"));
    }

    public void notifyNewBid(Auction auction, BidTransaction bid) {
        System.out.println("[NotificationService] Broadcasting NEW_BID — Auction: " +
                auction.getId() + ", Amount: $" + bid.getAmount() +
                ", Bidder: " + bid.getBidder().getUsername() +
                (bid.isAutoBid() ? " (AUTO)" : " (MANUAL)"));

        for (AuctionObserver observer : observers) {
            try {
                observer.onNewBid(auction, bid);
            } catch (Exception e) {
                System.err.println("[NotificationService] Observer error on onNewBid: " + e.getMessage());
            }
        }
    }

    public void notifyAuctionExtended(Auction auction, long extensionSeconds) {
        System.out.println("[NotificationService] Broadcasting AUCTION_EXTENDED — Auction: " +
                auction.getId() + ", Extended by " + extensionSeconds + "s, New endTime: " +
                auction.getEndTime());

        for (AuctionObserver observer : observers) {
            try {
                observer.onAuctionExtended(auction, extensionSeconds);
            } catch (Exception e) {
                System.err.println("[NotificationService] Observer error on onAuctionExtended: " + e.getMessage());
            }
        }
    }

    public void notifyAuctionEnded(Auction auction) {
        System.out.println("[NotificationService] Broadcasting AUCTION_ENDED — Auction: " +
                auction.getId() + ", Winner: " +
                (auction.getCurrentWinner() != null ? auction.getCurrentWinner().getUsername() : "NONE") +
                ", Final Price: $" + auction.getCurrentPrice());

        for (AuctionObserver observer : observers) {
            try {
                observer.onAuctionEnded(auction);
            } catch (Exception e) {
                System.err.println("[NotificationService] Observer error on onAuctionEnded: " + e.getMessage());
            }
        }
    }

    public int getObserverCount() {
        return observers.size();
    }
}
