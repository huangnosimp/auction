package vn.io.huangnosimp.service;

import vn.io.huangnosimp.database.AuctionDAO;
import vn.io.huangnosimp.database.BidDAO;
import vn.io.huangnosimp.database.UserDAO;
import vn.io.huangnosimp.model.Auction;
import vn.io.huangnosimp.model.AuctionStatus;
import vn.io.huangnosimp.model.BidTransaction;
import vn.io.huangnosimp.model.Bidder;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class BiddingService {


    private final AuctionDAO auctionDAO;
    private final BidDAO bidDAO;
    private final UserDAO userDAO;
    private final AuctionManager auctionManager;
    private final AuctionService auctionService;
    private final AutoBidService autoBidService;
    private final NotificationService notificationService;

    private final ConcurrentHashMap<String, ReentrantLock> auctionLocks = new ConcurrentHashMap<>();


    public BiddingService(AuctionDAO auctionDAO,
                          BidDAO bidDAO,
                          UserDAO userDAO,
                          AuctionService auctionService,
                          AutoBidService autoBidService,
                          NotificationService notificationService) {
        this.auctionDAO = auctionDAO;
        this.bidDAO = bidDAO;
        this.userDAO = userDAO;
        this.auctionManager = AuctionManager.getInstance();
        this.auctionService = auctionService;
        this.autoBidService = autoBidService;
        this.notificationService = notificationService;
    }

    public BidTransaction placeBid(String auctionId, String bidderId, double amount) {

        ReentrantLock lock = auctionLocks.computeIfAbsent(auctionId, id -> new ReentrantLock(true));

        lock.lock();
        try {
            Auction auction = auctionManager.getAuction(auctionId);
            if (auction == null) {
                throw new IllegalArgumentException("Auction not found or not active: " + auctionId);
            }

            Bidder bidder = userDAO.findBidderById(bidderId);
            if (bidder == null) {
                throw new IllegalArgumentException("Bidder not found: " + bidderId);
            }

            validateBid(auction, bidder, amount);

            auction.setCurrentPrice(amount);
            auction.setCurrentWinner(bidder);
            auction.setUpdatedAt(LocalDateTime.now());

            BidTransaction transaction = new BidTransaction(
                    auction,
                    bidder,
                    amount,
                    LocalDateTime.now(),
                    false
            );
            bidDAO.save(transaction);

            auctionDAO.update(auction);

            System.out.println("[BiddingService] BID ACCEPTED — Bidder: " +
                    bidder.getUsername() + ", Amount: $" + amount +
                    ", Auction: " + auctionId);

            boolean extended = auctionService.checkAndExtendAuction(auction);
            if (extended) {
                System.out.println("[BiddingService] Anti-sniping extension applied for auction " + auctionId);
            }

            notificationService.notifyNewBid(auction, transaction);

            autoBidService.processAutoBids(auction, transaction);

            return transaction;

        } finally {
            lock.unlock();
        }
    }

    private void validateBid(Auction auction, Bidder bidder, double amount) {

        if (auction.getStatus() != AuctionStatus.RUNNING) {
            throw new IllegalStateException(
                    "Cannot bid — auction status is " + auction.getStatus() +
                            " (must be RUNNING). Auction: " + auction.getId());
        }

        if (LocalDateTime.now().isAfter(auction.getEndTime())) {
            throw new IllegalStateException(
                    "Cannot bid — auction has expired. EndTime: " + auction.getEndTime());
        }

        if (auction.getSeller() != null &&
                auction.getSeller().getId().equals(bidder.getId())) {
            throw new IllegalStateException(
                    "Cannot bid — seller cannot bid on their own auction. " +
                            "Bidder: " + bidder.getUsername());
        }

        if (amount <= auction.getCurrentPrice()) {
            throw new IllegalStateException(
                    "Cannot bid — amount $" + amount +
                            " must be greater than current price $" + auction.getCurrentPrice());
        }

        if (auction.getCurrentWinner() != null &&
                auction.getCurrentWinner().getId().equals(bidder.getId())) {
            throw new IllegalStateException(
                    "Cannot bid — you are already the highest bidder. " +
                            "Bidder: " + bidder.getUsername());
        }
    }

    public void removeLock(String auctionId) {
        auctionLocks.remove(auctionId);
    }

    public int getActiveLockCount() {
        return auctionLocks.size();
    }
}
