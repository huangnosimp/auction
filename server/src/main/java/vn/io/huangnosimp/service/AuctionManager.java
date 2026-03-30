package vn.io.huangnosimp.service;

import vn.io.huangnosimp.model.Auction;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class AuctionManager {

    private static volatile AuctionManager instance;

    public static AuctionManager getInstance() {
        if (instance == null) {
            synchronized (AuctionManager.class) {
                if (instance == null) {
                    instance = new AuctionManager();
                }
            }
        }
        return instance;
    }

    private AuctionManager() {
        this.activeAuctions = new ConcurrentHashMap<>();
    }

    private final ConcurrentHashMap<String, Auction> activeAuctions;

    public void addAuction(Auction auction) {
        if (auction == null || auction.getId() == null) {
            throw new IllegalArgumentException("Auction and its ID must not be null");
        }
        activeAuctions.put(auction.getId(), auction);
        System.out.println("[AuctionManager] Auction registered: " + auction.getId());
    }

    public Auction getAuction(String auctionId) {
        return activeAuctions.get(auctionId);
    }

    public Auction removeAuction(String auctionId) {
        Auction removed = activeAuctions.remove(auctionId);
        if (removed != null) {
            System.out.println("[AuctionManager] Auction removed: " + auctionId);
        }
        return removed;
    }

    public boolean isActive(String auctionId) {
        return activeAuctions.containsKey(auctionId);
    }

    public Collection<Auction> getAllActiveAuctions() {
        return activeAuctions.values();
    }

    public int getActiveCount() {
        return activeAuctions.size();
    }

    static void resetInstance() {
        synchronized (AuctionManager.class) {
            instance = null;
        }
    }
}
