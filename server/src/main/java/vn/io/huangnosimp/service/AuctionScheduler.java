package vn.io.huangnosimp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vn.io.huangnosimp.model.Auction;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class AuctionScheduler {
    private static final Logger logger = LoggerFactory.getLogger(AuctionScheduler.class);
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
        logger.info("Auction scheduled auctionId={} startTime={} endTime={}",
                auction.getId(), auction.getStartTime(), auction.getEndTime());
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
        logger.debug("Auction start scheduled auctionId={} delayMs={}", auctionId, delayToStart);
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
        logger.debug("Auction finish scheduled auctionId={} delayMs={}", auctionId, delayToEnd);
        if (endTask.isDone()) {
            endTimer.remove(auctionId, endTask);
        }
    }

    public boolean extendTime(Auction auction, long newEndTime) {
        String auctionId = auction.getId();
        ScheduledFuture<?> oldTask = endTimer.remove(auctionId);
        if (oldTask == null) {
            logger.warn("Auction end time extension skipped because end timer was not found auctionId={}", auctionId);
            return false;
        }
        if (!auction.extendEndTime(newEndTime)) {
            logger.warn("Auction end time extension rejected auctionId={} newEndTime={}", auctionId, newEndTime);
            endTimer.put(auctionId, oldTask);
            return false;
        }

        if (!oldTask.isDone()) {
            oldTask.cancel(false);
        }
        this.scheduleEnd(auction);
        logger.info("Auction end time extended auctionId={} newEndTime={}", auctionId, newEndTime);
        return true;
    }

    public void cancelTimers(String auctionId) {
        ScheduledFuture<?> startTask = startTimer.remove(auctionId);
        if (startTask != null && !startTask.isDone()) {
            startTask.cancel(false);
            logger.debug("Auction start timer canceled auctionId={}", auctionId);
        }
        ScheduledFuture<?> endTask = endTimer.remove(auctionId);
        if (endTask != null && !endTask.isDone()) {
            endTask.cancel(false);
            logger.debug("Auction finish timer canceled auctionId={}", auctionId);
        }
        logger.info("Auction timers canceled auctionId={}", auctionId);
    }

    private void startAuctionTask(String auctionId) {
        if (auctionService instanceof AuctionService) {
            logger.info("Auction start task executing auctionId={}", auctionId);
            ((AuctionService) auctionService).startAuction(auctionId);
        } else {
            logger.warn("Auction start task skipped because service type is unsupported auctionId={}", auctionId);
        }
    }

    private void finishAuctionTask(String auctionId) {
        if (auctionService instanceof AuctionService) {
            logger.info("Auction finish task executing auctionId={}", auctionId);
            ((AuctionService) auctionService).finishAuction(auctionId);
        } else {
            logger.warn("Auction finish task skipped because service type is unsupported auctionId={}", auctionId);
        }
    }

    public static void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            logger.info("Auction scheduler shutdown requested");
            try {
                if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                    logger.warn("Auction scheduler forced shutdown after timeout");
                }
            } catch (InterruptedException ex) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
                logger.warn("Auction scheduler shutdown interrupted", ex);
            }
        }
    }
}
