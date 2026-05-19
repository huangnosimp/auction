package vn.io.huangnosimp.service;

import vn.io.huangnosimp.dto.response.BidResult;
import vn.io.huangnosimp.enums.AuctionStatus;
import vn.io.huangnosimp.model.AutoBidConfig;
import vn.io.huangnosimp.model.Auction;
import vn.io.huangnosimp.model.Member;
import vn.io.huangnosimp.model.User;
import vn.io.huangnosimp.repository.IAutoBidRepository;
import vn.io.huangnosimp.repository.IAuctionRepository;
import vn.io.huangnosimp.repository.IUserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public class AutoBidService implements IAutoBidService {
    private final IAutoBidRepository autoBidRepository;
    private final IUserRepository userRepository;
    private final IAuctionRepository auctionRepository;
    private IAuctionService auctionService;

    public AutoBidService (IAutoBidRepository autoBidRepository,
                              IUserRepository userRepository,
                              IAuctionRepository auctionRepository) {
        this.autoBidRepository = autoBidRepository;
        this.userRepository = userRepository;
        this.auctionRepository = auctionRepository;
    }

    public void setAuctionService(IAuctionService auctionService) {
        this.auctionService = auctionService;
    }

    @Override
    public boolean registerAutoBid(String bidderId, String auctionId, double maxBid, double increment) {
        User user = userRepository.findById(bidderId);
        if (!(user instanceof Member bidder)) {
            throw new IllegalArgumentException("User is not a valid member.");
        }

        Auction auction = auctionRepository.findById(auctionId);
        if (auction == null) {
            throw new IllegalArgumentException("Auction not found.");
        }

        if (auction.getStatus() != AuctionStatus.RUNNING) {
            throw new IllegalStateException("Auction is not currently running.");
        }

        if (increment <= 0) {
            throw new IllegalArgumentException("Increment must be greater than 0.");
        }

        if (auction.getMinimumIncrement() > 0 && increment < auction.getMinimumIncrement()) {
            throw new IllegalArgumentException("Increment must be at least " + auction.getMinimumIncrement());
        }

        if (maxBid <= auction.getCurrentPrice()) {
            throw new IllegalArgumentException("Maximum bid must be higher than the current price.");
        }

        if (bidder.getAccountBalance() < maxBid) {
            throw new IllegalArgumentException("Insufficient available balance to set this Auto-Bid limit.");
        }

        AutoBidConfig existingConfig = autoBidRepository.findByMemberAndAuction(bidderId, auctionId);
        if (existingConfig != null) {
            existingConfig.setMaxBid(maxBid);
            existingConfig.setIncrement(increment);
            existingConfig.setRegisteredAt(LocalDateTime.now());
            autoBidRepository.save(existingConfig);
            System.out.println("[AutoBid] User " + bidder.getUsername() + " UPDATED AutoBid for Auction " + auctionId);
        } else {
            AutoBidConfig config = new AutoBidConfig(bidder, auction, maxBid, increment, LocalDateTime.now());
            autoBidRepository.save(config);
            System.out.println("[AutoBid] User " + bidder.getUsername() + " CREATED AutoBid for Auction " + auctionId);
        }

        processAutoBids(auctionId);

        return true;
    }

    @Override
    public synchronized void processAutoBids(String auctionId) {
        synchronized (auctionId.intern()) {
            boolean keepBidding = true;

            while (keepBidding) {
                keepBidding = false;

                List<AutoBidConfig> configs = autoBidRepository.findByAuctionId(auctionId);
                if (configs == null || configs.isEmpty()) return;
                configs.sort(Comparator.comparing(AutoBidConfig::getRegisteredAt));

                Auction auction = auctionRepository.findById(auctionId);
                if (auction == null || !"RUNNING".equalsIgnoreCase(String.valueOf(auction.getStatus()))) return;

                double currentPrice = auction.getCurrentPrice();
                String currentWinnerId = auction.getCurrentWinnerId();

                for (AutoBidConfig config : configs) {
                    if (config.getBidder().getId().equals(currentWinnerId)) continue;

                    double nextBid = currentPrice + config.getIncrement();

                    if (nextBid <= config.getMaxBid()) {
                        BidResult result = auctionService.placeBid(config.getBidder().getId(), auctionId, nextBid, false);

                        if (result == BidResult.SUCCESS) {
                            System.out.println("[AutoBid] " + config.getBidder().getUsername() + " auto-bidded $" + nextBid);
                            keepBidding = true;
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void unregisterAutoBid(String bidderId, String auctionId) {
        AutoBidConfig existing = autoBidRepository.findByMemberAndAuction(bidderId, auctionId);

        if (existing == null) {
            throw new IllegalArgumentException("No active Auto-Bid found for this auction.");
        }

        try {
            autoBidRepository.delete(bidderId, auctionId);
            System.out.println("[AutoBid] Unregistered for user: " + bidderId + " on auction: " + auctionId);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to unregister Auto-Bid due to database error.");
        }
    }
}