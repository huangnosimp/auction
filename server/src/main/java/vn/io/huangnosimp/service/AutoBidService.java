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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoBidService implements IAutoBidService {
    private static final Logger logger = LoggerFactory.getLogger(AutoBidService.class);
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
            logger.warn("Autobid registration rejected because bidder was not found bidderId={} auctionId={}", bidderId, auctionId);
            return false;
        }

        Auction auction = auctionRepository.findById(auctionId);

        if (auction == null) {
            logger.warn("Autobid registration rejected because auction was not found bidderId={} auctionId={}", bidderId, auctionId);
            return false;
        }
        if (increment <= 0) {
            logger.warn("Autobid registration rejected due to invalid increment bidderId={} auctionId={} increment={}",
                    bidderId, auctionId, increment);
            return false;
        }

        if (maxBid <= auction.getCurrentPrice()) {
            logger.info("Autobid registration rejected because max bid is too low bidderId={} auctionId={} maxBid={} currentPrice={}",
                    bidderId, auctionId, maxBid, auction.getCurrentPrice());
            return false;
        }

        AutoBidConfig config = new AutoBidConfig(bidder, auction, maxBid, increment, LocalDateTime.now());
        autoBidRepository.save(config);

        logger.info("Registered autobid bidderId={} auctionId={}", bidderId, auctionId);
        return true;
    }

    @Override
    public void processAutoBids(String auctionId) {
        List<AutoBidConfig> configs = autoBidRepository.findByAuctionId(auctionId);
        if (configs == null || configs.isEmpty()) {
            logger.debug("No autobid configs found auctionId={}", auctionId);
            return;
        }

        configs.sort(Comparator.comparing(AutoBidConfig::getRegisteredAt));

        Auction auction = auctionRepository.findById(auctionId);
        if (auction == null) {
            logger.warn("Autobid processing skipped because auction was not found auctionId={}", auctionId);
            return;
        }

        double currentPrice = auction.getCurrentPrice();
        String currentWinnerId = auction.getCurrentWinnerId();

        for (AutoBidConfig config : configs) {
            if (config.getBidder().getId().equals(currentWinnerId)) continue;

            double nextBid = currentPrice + config.getIncrement();

            if (nextBid <= config.getMaxBid()) {
                BidResult result = auctionService.placeBid(config.getBidder().getId(), auctionId, nextBid, false);

                if (result == BidResult.SUCCESS) {
                    logger.info("Autobid placed successfully bidderId={} auctionId={}", config.getBidder().getId(), auctionId);
                    break;
                } else {
                    logger.info(
                            "Autobid placement skipped bidderId={} auctionId={} result={}",
                            config.getBidder().getId(), auctionId, result
                    );
                }
            }
        }
    }

    @Override
    public void unregisterAutoBid(String bidderId, String auctionId) {
        AutoBidConfig existing = autoBidRepository.findByMemberAndAuction(bidderId, auctionId);

        if (existing != null) {
            autoBidRepository.delete(bidderId, auctionId);
            logger.info("Unregistered autobid bidderId={} auctionId={}", bidderId, auctionId);
        } else {
            logger.debug("Autobid unregister skipped because config was not found bidderId={} auctionId={}", bidderId, auctionId);
        }
    }
}
