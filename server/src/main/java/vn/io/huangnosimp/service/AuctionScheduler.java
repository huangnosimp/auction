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
        auction.setStatusOPEN();
        long delayToStart = auction.getStartTime() - System.currentTimeMillis();
        String auctionId = auction.getId();
        ScheduledFuture<?> startTask = scheduler.schedule(() -> {
            this.startAuctionTask(auction);
            this.scheduleEnd(auction);
            startTimer.remove(auctionId);
        }, delayToStart, TimeUnit.MILLISECONDS);
        startTimer.put(auctionId, startTask);
    }

    private void scheduleEnd(Auction auction) {
        long delayToEnd = auction.getEndTime() - System.currentTimeMillis();
        String auctionId = auction.getId();
        ScheduledFuture<?> endTask = scheduler.schedule(() -> {
            this.finishAuctionTask(auction);
            endTimer.remove(auctionId);
        }, delayToEnd, TimeUnit.MILLISECONDS);
        endTimer.put(auctionId, endTask);
    }

    public void extendTime(Auction auction, long newEndTime) {
        String auctionId = auction.getId();
        ScheduledFuture<?> oldTask = endTimer.remove(auctionId);
        if (oldTask != null && !oldTask.isDone()) {
            oldTask.cancel(false);
        }
        auction.extendEndTime(newEndTime);
        this.scheduleEnd(auction);
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

    private void startAuctionTask(Auction auction) {
        auction.setStatusRunning();
    }

    private void finishAuctionTask(Auction auction) {
        auction.setStatusFinish();
        if (auctionService instanceof AuctionService) {
            ((AuctionService) auctionService).processPayment(auction.getId());
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
