package vn.io.huangnosimp.service;

import vn.io.huangnosimp.network.AuctionDTO;
import vn.io.huangnosimp.model.*;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.Collections;
import java.util.Map;

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

    public Map<String, Auction> getAuctions() {
        return Collections.unmodifiableMap(auctions);
    }

    public Auction openAuction(String sellerId, String itemId, double startPrice, long startTime, long endTime) {
        if (!isValidOpenAuctionInput(sellerId, itemId, startPrice) || endTime <= startTime) {
            return null;
        }

        Seller seller = UserService.getInstance().getSeller(sellerId);
        Item item = ItemService.getInstance().getItem(itemId);
        if (seller == null || item == null) {
            return null;
        }

        Auction auction = new Auction(item, seller, startPrice, startTime, endTime);
        seller.addAuction(auction.getId());
        auctions.put(auction.getId(), auction);
        this.scheduleAuction(auction.getId(), startTime, endTime);
        return auction;
    }

    public Auction openAuction(String sellerId, String itemId, double startPrice, int durationInMinutes) {
        if (!isValidOpenAuctionInput(sellerId, itemId, startPrice) || durationInMinutes <= 0) {
            return null;
        }

        Seller seller = UserService.getInstance().getSeller(sellerId);
        Item item = ItemService.getInstance().getItem(itemId);
        if (seller == null || item == null) {
            return null;
        }

        Auction auction = new Auction(item, seller, startPrice, durationInMinutes);
        seller.addAuction(auction.getId());
        auctions.put(auction.getId(), auction);
        long startTime = System.currentTimeMillis();
        long endTime = startTime + ((long) durationInMinutes * 60 * 1000);
        this.scheduleAuction(auction.getId(), startTime, endTime);
        return auction;
    }

    private boolean isValidOpenAuctionInput(String sellerId, String itemId, double startPrice) {
        return sellerId != null && !sellerId.isBlank() && itemId != null && !itemId.isBlank() && startPrice > 0;
    }

    public boolean placeBid(String bidderId, String auctionId, double amount) {
        Auction auction = auctions.get(auctionId);
        if (auction == null) {
            return false;
        }
        Bidder bidder = UserService.getInstance().getBidder(bidderId);
        if (bidder == null) {
            return false;
        }
        synchronized (auction) {
            boolean isSuccess = auction.placeBid(bidder, amount);
            if (isSuccess) {
                auction.setUpdatedAt(System.currentTimeMillis());
                //save all status of auction to database
                if (auction.needExtension()) {
                    this.extendAuctionTime(auctionId);
                }
            }
            return isSuccess;
        }
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
        Auction auction = auctions.get(auctionId);
        if (auction != null) {
            synchronized (auction) {
                auction.setStatusOPEN();
            }
        }
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
                auction.setStatusRunning();
            }
        }
    }

    public void finishAuctionTask(String auctionId) {
        Auction auction = auctions.get(auctionId);
        if (auction != null) {
            synchronized (auction) {
                auction.setStatusFinish();
            }   
        }
    }

    public void extendAuctionTime(String auctionId) {
        Auction auction = auctions.get(auctionId);
        if (auction != null) {
            synchronized (auction) {
                if (auction.getStatus() != AuctionStatus.RUNNING) {
                    return;
                }
                ScheduledFuture<?> oldTask = endTimer.remove(auctionId);
                if (oldTask != null && !oldTask.isDone()) {
                    oldTask.cancel(false);
                }
                long newEndTime = System.currentTimeMillis() + 60 * 1000;
                this.scheduleAuctionEnd(auctionId, newEndTime);
                auction.extendEndTime(newEndTime);
            }
        }
    }

    public boolean joinAuction(String auctionId, String bidderId) {
        Auction auction = auctions.get(auctionId);
        Bidder bidder = UserService.getInstance().getBidder(bidderId);
        if (auction == null || bidder == null) {
            return false;
        }
        bidder.joinRoom(auctionId);
        return true;
    }

    public boolean leaveAuction(String auctionId, String bidderId) {
        Auction auction = auctions.get(auctionId);
        Bidder bidder = UserService.getInstance().getBidder(bidderId);
        if (auction == null || bidder == null) {
            return false;
        }
        bidder.leaveRoom(auctionId);
        return true;
    }

    public boolean cancelAuction(String auctionId) {
        Auction auction = auctions.get(auctionId);
        if (auction == null) {
            return false;
        }
        synchronized (auction) {
            if (auction.getStatus() == AuctionStatus.OPEN || auction.getStatus() == AuctionStatus.RUNNING) {
                String currentWinnerId = auction.getCurrentWinnerId();
                if (currentWinnerId != null) {
                    Bidder currentWinner = auction.getBidder(currentWinnerId);
                    if (currentWinner != null) {
                        currentWinner.unfreezeMoney(auction.getCurrentPrice());
                    }
                }
                auction.setStatusCanceled();
                ScheduledFuture<?> startTask = startTimer.remove(auctionId);
                if (startTask != null && !startTask.isDone()) {
                    startTask.cancel(false);
                }
                ScheduledFuture<?> endTask = endTimer.remove(auctionId);
                if (endTask != null && !endTask.isDone()) {
                    endTask.cancel(false);
                }
                return true;
            }
            return false;
        }
    }

    public boolean processPayment(String auctionId) {
        Auction auction = auctions.get(auctionId);
        if (auction == null) {
            return false;
        }
        synchronized (auction) {
            if (auction.getStatus() == AuctionStatus.FINISHED) {
                String bidderId = auction.getCurrentWinnerId();
                if (bidderId == null) {
                    return false;
                }
                Bidder winner = auction.getBidder(bidderId);
                if (winner != null) {
                    if (!winner.deductFrozenMoney(auction.getCurrentPrice())) {
                        return false;
                    }
                    BidTransaction bidTransaction = new BidTransaction(auctionId, bidderId, auction.getCurrentPrice(), false);
                    //save bidTransaction to database
                    auction.setStatusPaid();
                    return true;
                }
            }
        }
        return false;
    }

    public ArrayList<AuctionDTO> getAuction() {
        ArrayList<AuctionDTO> runningAuctions = new ArrayList<>();
        for (Auction auction : auctions.values()) {
            if (auction.getStatus() == AuctionStatus.RUNNING || auction.getStatus() == AuctionStatus.OPEN) {
                runningAuctions.add(new AuctionDTO(auction, UserService.getInstance().getBidderUserName(auction.getCurrentWinnerId())));
            }
        }
        return runningAuctions;
    }

    public ArrayList<AuctionDTO> getAuctionBySeller(String SellerId) {
        ArrayList<AuctionDTO> sellerAuctions = new ArrayList<>();
        for (Auction auction : auctions.values()) {
            if (auction.getSeller().getId().equals(SellerId)) {
                sellerAuctions.add(new AuctionDTO(auction, UserService.getInstance().getBidderUserName(auction.getCurrentWinnerId())));
            }
        }
        return sellerAuctions;
    }

    public AuctionDTO getAuctionDetail(String auctionId) {
        for (Auction auction : auctions.values()) {
            if (auction.getId().equals(auctionId)) {
                return new AuctionDTO(auction, UserService.getInstance().getBidderUserName(auction.getCurrentWinnerId()));
            }
        }
        return null;
    }
}
