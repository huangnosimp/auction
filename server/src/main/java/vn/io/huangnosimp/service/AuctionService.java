package vn.io.huangnosimp.service;

import vn.io.huangnosimp.database.AuctionDAO;
import vn.io.huangnosimp.model.Auction;
import vn.io.huangnosimp.model.AuctionStatus;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class AuctionService {

    private static final long SNIPING_THRESHOLD_SECONDS = 30;

    private static final long SNIPING_EXTENSION_SECONDS = 60;


    private final AuctionDAO auctionDAO;
    private final AuctionManager auctionManager;
    private final NotificationService notificationService;

    public AuctionService(AuctionDAO auctionDAO,
                          NotificationService notificationService) {
        this.auctionDAO = auctionDAO;
        this.auctionManager = AuctionManager.getInstance();
        this.notificationService = notificationService;
    }

    public void createAuction(Auction auction) {
        if (auction == null) {
            throw new IllegalArgumentException("Auction must not be null");
        }
        auction.setStatus(AuctionStatus.OPEN);
        auction.setUpdatedAt(LocalDateTime.now());

        auctionDAO.save(auction);
        auctionManager.addAuction(auction);

        System.out.println("[AuctionService] Auction created: " + auction.getId());
    }

    public void startAuction(String auctionId) {
        Auction auction = getActiveAuctionOrThrow(auctionId);

        if (auction.getStatus() != AuctionStatus.OPEN) {
            throw new IllegalStateException(
                    "Cannot start auction " + auctionId + " — current status: " + auction.getStatus());
        }

        auction.setStatus(AuctionStatus.RUNNING);
        auction.setUpdatedAt(LocalDateTime.now());
        auctionDAO.update(auction);

        System.out.println("[AuctionService] Auction RUNNING: " + auctionId);
    }

    public void endAuction(String auctionId) {
        Auction auction = getActiveAuctionOrThrow(auctionId);

        auction.setStatus(AuctionStatus.FINISHED);
        auction.setUpdatedAt(LocalDateTime.now());
        auctionDAO.update(auction);

        auctionManager.removeAuction(auctionId);

        notificationService.notifyAuctionEnded(auction);

        System.out.println("[AuctionService] Auction FINISHED: " + auctionId +
                ", Winner: " + (auction.getCurrentWinner() != null
                ? auction.getCurrentWinner().getUsername() : "NONE"));
    }

    public boolean checkAndExtendAuction(Auction auction) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = auction.getEndTime();

        long secondsRemaining = ChronoUnit.SECONDS.between(now, endTime);

        if (secondsRemaining > 0 && secondsRemaining <= SNIPING_THRESHOLD_SECONDS) {
            LocalDateTime newEndTime = endTime.plusSeconds(SNIPING_EXTENSION_SECONDS);
            auction.setEndTime(newEndTime);
            auction.setUpdatedAt(LocalDateTime.now());

            auctionDAO.update(auction);

            notificationService.notifyAuctionExtended(auction, SNIPING_EXTENSION_SECONDS);

            System.out.println("[AuctionService] ANTI-SNIPING triggered for auction " +
                    auction.getId() + " — extended by " + SNIPING_EXTENSION_SECONDS +
                    "s, new endTime: " + newEndTime);
            return true;
        }

        return false;
    }

    private Auction getActiveAuctionOrThrow(String auctionId) {
        Auction auction = auctionManager.getAuction(auctionId);
        if (auction == null) {
            throw new IllegalArgumentException("No active auction found with ID: " + auctionId);
        }
        return auction;
    }


    public static long getSnipingThresholdSeconds() {
        return SNIPING_THRESHOLD_SECONDS;
    }

    public static long getSnipingExtensionSeconds() {
        return SNIPING_EXTENSION_SECONDS;
    }
}
