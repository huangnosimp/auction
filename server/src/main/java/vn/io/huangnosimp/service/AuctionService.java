package vn.io.huangnosimp.service;
import vn.io.huangnosimp.model.Auction;
import vn.io.huangnosimp.model.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;

public class AuctionService {
    private static volatile AuctionService instance;
    private final ConcurrentHashMap<String, Auction> auctions;
    private final ConcurrentHashMap<String, ScheduledFuture<?>> startTimer;
    private final ConcurrentHashMap<String, ScheduledFuture<?>> endTimer;
    private final ScheduledExecutorService scheduler;

    private AuctionService() {
        auctions = new ConcurrentHashMap<>();
        startTimer = new ConcurrentHashMap<>();
        endTimer = new ConcurrentHashMap<>();
        scheduler = Executors.newScheduledThreadPool(10);
    }
    
    public static AuctionService getInstance() {
        if (instance == null) {
            synchronized (AuctionService.class) {
                if (instance == null) {
                    instance = new AuctionService();
                }
            }
        }
        return instance;
    }

    public void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException ex) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    public ConcurrentHashMap<String, Auction> getAuctions() {
        return auctions;
    }

    public Auction openAuction(String sellerId, String itemId, double startPrice, long startTime, long endTime) {
        Auction auction = new Auction(itemId, sellerId, startPrice, startTime, endTime);
        auctions.put(auction.getId(), auction);
        this.scheduleAuction(auction.getId(), startTime, endTime);
        return auction;
    }
    
    public Auction openAuction(String sellerId, String itemId, double startPrice, int durationInMinutes) {
        Auction auction = new Auction(itemId, sellerId, startPrice, durationInMinutes);
        auctions.put(auction.getId(), auction);
        long startTime = System.currentTimeMillis();
        long endTime = startTime + ((long) durationInMinutes * 60 * 1000);
        this.scheduleAuction(auction.getId(), startTime, endTime);
        return auction;
    }
    
    public boolean placeBid(String bidderId, String auctionId, double amount) {
        Auction auction = auctions.get(auctionId);
        if (auction == null) {
            return false;
        }
        
        synchronized (auction) {
            if (auction.getStatus() != AuctionStatus.RUNNING) {
                return false;
            }
            if (amount <= auction.getCurrentPrice()) {
                return false;
            }
            auction.setCurrentPrice(amount);
            auction.setCurrentWinnerId(bidderId);
            BidTransaction bidTransaction = new BidTransaction(auctionId, bidderId, amount, false);
            // Save bidTransaction to persistent storage
        }
        return true;
    }

    private void scheduleAuction(String auctionId, long startTime, long endTime) {
        long currentTime = System.currentTimeMillis();

        if (currentTime < startTime) {
            this.scheduleAuctionStart(auctionId, startTime, endTime);
        } else if (currentTime >= startTime && currentTime < endTime) {
            this.startAuctionTask(auctionId);
            this.scheduleAuctionEnd(auctionId, endTime);
        } else {
            this.finishAuctionTask(auctionId);
        }
    }

    private void scheduleAuctionStart(String auctionId, long startTime, long endTime) {
        long delayToStart = startTime - System.currentTimeMillis();
        ScheduledFuture<?> startTask = scheduler.schedule(() -> {
            this.startAuctionTask(auctionId);
            this.scheduleAuctionEnd(auctionId, endTime);
            startTimer.remove(auctionId);
        }, delayToStart, TimeUnit.MILLISECONDS);
        startTimer.put(auctionId, startTask);
    }
    
    private void scheduleAuctionEnd(String auctionId, long endTime) {
        long delayToEnd = endTime - System.currentTimeMillis();
        ScheduledFuture<?> endTask = scheduler.schedule(() -> {
            this.finishAuctionTask(auctionId);
            endTimer.remove(auctionId);
        }, delayToEnd, TimeUnit.MILLISECONDS);
        endTimer.put(auctionId, endTask);
    }

    public void startAuctionTask(String auctionId) {
        Auction auction = auctions.get(auctionId);
        if (auction != null) {
            synchronized (auction) {
                if (auction.getStatus() == AuctionStatus.OPEN) {
                    auction.setStatus(AuctionStatus.RUNNING);
                }
            }
        }
    }
    
    public void finishAuctionTask(String auctionId) {
        Auction auction = auctions.get(auctionId);
        if (auction != null) {
            synchronized (auction) {
                if (auction.getStatus() == AuctionStatus.RUNNING) {
                    auction.setStatus(AuctionStatus.FINISHED);
                }
            }   
        }
    }
}
