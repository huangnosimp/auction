package vn.io.huangnosimp.service;

import vn.io.huangnosimp.dto.response.BidResult;
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
        if (!(user instanceof Member bidder)) return false;

        Auction auction = auctionRepository.findById(auctionId);

        if (bidder == null || auction == null) return false;
        if (increment <= 0) return false;

        if (maxBid <= auction.getCurrentPrice()) return false;

        AutoBidConfig config = new AutoBidConfig(bidder, auction, maxBid, increment, LocalDateTime.now());
        autoBidRepository.save(config);

        System.out.println("[AutoBid] User " + bidder.getUsername() + " set AutoBid for Auction " + auctionId);
        return true;
    }

    @Override
    public void processAutoBids(String auctionId) {
        List<AutoBidConfig> configs = autoBidRepository.findByAuctionId(auctionId);
        if (configs == null || configs.isEmpty()) return;

        configs.sort(Comparator.comparing(AutoBidConfig::getRegisteredAt));

        Auction auction = auctionRepository.findById(auctionId);
        if (auction == null) return;

        double currentPrice = auction.getCurrentPrice();
        String currentWinnerId = auction.getCurrentWinnerId();

        for (AutoBidConfig config : configs) {
            if (config.getBidder().getId().equals(currentWinnerId)) continue;

            double nextBid = currentPrice + config.getIncrement();

            if (nextBid <= config.getMaxBid()) {
                BidResult result = auctionService.placeBid(config.getBidder().getId(), auctionId, nextBid, false);

                if (result == BidResult.SUCCESS) {
                    System.out.println("[AutoBid] Success for " + config.getBidder().getUsername());
                    break;
                } else {
                    System.out.println("[AutoBid] Failed for " + config.getBidder().getUsername() + " (e.g., Not enough money)");
                }
            }
        }
    }

    @Override
    public void unregisterAutoBid(String bidderId, String auctionId) {
        AutoBidConfig existing = autoBidRepository.findByMemberAndAuction(bidderId, auctionId);

        if (existing != null) {
            autoBidRepository.delete(bidderId, auctionId);
            System.out.println("[AutoBid] Unregistered for user: " + bidderId + " on auction: " + auctionId);
        }
    }
}