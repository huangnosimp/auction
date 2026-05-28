package vn.io.huangnosimp.service;

import vn.io.huangnosimp.dto.response.AutoBidResponseDTO;
import vn.io.huangnosimp.dto.response.BidResult;
import vn.io.huangnosimp.model.AutoBidConfig;
import vn.io.huangnosimp.model.Auction;
import vn.io.huangnosimp.model.Member;
import vn.io.huangnosimp.model.User;
import vn.io.huangnosimp.repository.IAutoBidRepository;
import vn.io.huangnosimp.repository.IAuctionRepository;
import vn.io.huangnosimp.repository.IUserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.io.huangnosimp.util.ModelMapper;

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

        processAutoBids(auctionId);

        logger.info("Registered autobid bidderId={} auctionId={}", bidderId, auctionId);
        return true;
    }

    public void processAutoBids(String auctionId) {
        List<AutoBidConfig> configs = autoBidRepository.findByAuctionId(auctionId);
        if (configs == null || configs.isEmpty()) {
            return;
        }

        Auction auction = auctionRepository.findById(auctionId);
        if (auction == null) {
            return;
        }

        configs.sort(Comparator.comparing(AutoBidConfig::getMaxBid).reversed()
                .thenComparing(AutoBidConfig::getRegisteredAt));

        double currentPrice = auction.getCurrentPrice();
        String currentWinnerId = auction.getCurrentWinnerId();

        if (configs.size() == 1) {
            AutoBidConfig loneBot = configs.get(0);

            if (!loneBot.getBidder().getId().equals(currentWinnerId)) {
                double nextBid = currentPrice + loneBot.getIncrement();

                if (nextBid <= loneBot.getMaxBid()) {
                    auctionService.placeBid(loneBot.getBidder().getId(), auctionId, nextBid, false);
                }
            }
            return;
        }

        AutoBidConfig top1 = configs.get(0);
        AutoBidConfig top2 = configs.get(1);

        if (top1.getBidder().getId().equals(currentWinnerId) && currentPrice >= top2.getMaxBid()) {
            return;
        }

        double basePriceToBeat = Math.max(currentPrice, top2.getMaxBid());
        double jumpPrice = basePriceToBeat + top1.getIncrement();

        if (jumpPrice > top1.getMaxBid()) {
            jumpPrice = top1.getMaxBid();
        }

        if (jumpPrice > currentPrice && jumpPrice <= top1.getMaxBid()) {
            BidResult result = auctionService.placeBid(top1.getBidder().getId(), auctionId, jumpPrice, false);

            if (result == BidResult.SUCCESS) {
                logger.info("Bot war resolved seamlessly. Top1 won at FinalPrice={}", jumpPrice);
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

    @Override
    public List<AutoBidResponseDTO> getAutoBidsByUserId(String userId) {
        List<AutoBidConfig> configs = autoBidRepository.findByUserId(userId);

        if (configs != null && !configs.isEmpty()) {
            logger.info("Fetched {} autobid configs for userId={}", configs.size(), userId);
            return ModelMapper.toAutoBidResponseDTOList(configs);
        } else {
            logger.debug("No autobid configs found for userId={}", userId);
            return new ArrayList<>();
        }
    }

}
