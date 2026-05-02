package vn.io.huangnosimp.service;

import vn.io.huangnosimp.model.Auction;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class AuctionScheduler {
    private final ConcurrentHashMap<String, ScheduledFuture<?>> startTimer;
    private final ConcurrentHashMap<String, ScheduledFuture<?>> endTimer;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    private final IAuctionService auctionService;

    public AuctionScheduler(IAuctionService auctionService) {
        this.startTimer = new ConcurrentHashMap<>();
        this.endTimer = new ConcurrentHashMap<>();
        this.auctionService = auctionService;
    }

    public void scheduleAuction(Auction auction) {
        this.scheduleStart(auction);
    }

    private void scheduleStart(Auction auction) {
        long delayToStart = Math.max(0, auction.getStartTime() - System.currentTimeMillis());
        String auctionId = auction.getId();
        
        ScheduledFuture<?> startTask = scheduler.schedule(() -> {
            this.startAuctionTask(auctionId);
            this.scheduleEnd(auction);
            startTimer.remove(auctionId);
        }, delayToStart, TimeUnit.MILLISECONDS);
        
        startTimer.put(auctionId, startTask);
        if (startTask.isDone()) {
            startTimer.remove(auctionId, startTask);
        }
    }

    private void scheduleEnd(Auction auction) {
        long delayToEnd = Math.max(0, auction.getEndTime() - System.currentTimeMillis());
        String auctionId = auction.getId();
        
        ScheduledFuture<?> endTask = scheduler.schedule(() -> {
            this.finishAuctionTask(auctionId);
            endTimer.remove(auctionId);
        }, delayToEnd, TimeUnit.MILLISECONDS);
        
        endTimer.put(auctionId, endTask);
        if (endTask.isDone()) {
            endTimer.remove(auctionId, endTask);
        }
    }

    public void extendTime(Auction auction, long newEndTime) {
        String auctionId = auction.getId();
        auction.extendEndTime(newEndTime);
        
        ScheduledFuture<?> oldTask = endTimer.remove(auctionId);
        if (oldTask != null) {
            if (!oldTask.isDone()) {
                oldTask.cancel(false);
            }
            this.scheduleEnd(auction);
        }
    }

    public void cancelTimers(String auctionId) {
        ScheduledFuture<?> startTask = startTimer.remove(auctionId);
        if (startTask != null && !startTask.isDone()) {
            startTask.cancel(false);
        }
        ScheduledFuture<?> endTask = endTimer.remove(auctionId);
        if (endTask != null && !endTask.isDone()) {
            endTask.cancel(false);
        }
    }

    private void startAuctionTask(String auctionId) {
        if (auctionService instanceof AuctionService) {
            ((AuctionService) auctionService).startAuction(auctionId);
        }
    }

    private void finishAuctionTask(String auctionId) {
        if (auctionService instanceof AuctionService) {
            ((AuctionService) auctionService).finishAuction(auctionId);
        }
    }

    public static void shutdown() {
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
}
