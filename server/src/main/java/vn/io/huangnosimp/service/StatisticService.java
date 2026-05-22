package vn.io.huangnosimp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vn.io.huangnosimp.dto.response.AuctionCardDTO;
import vn.io.huangnosimp.dto.response.AuctionDetailResponseDTO;
import vn.io.huangnosimp.dto.response.DashboardResponseDTO;
import vn.io.huangnosimp.repository.*;

import java.util.List;
import java.util.Collections;

public class StatisticService implements IStatisticService {
    private static final Logger logger = LoggerFactory.getLogger(StatisticService.class);
    private final IStatisticRepository statisticRepository;

    public StatisticService(IStatisticRepository statisticRepository) {
        this.statisticRepository = statisticRepository;
    }

    @Override
    public DashboardResponseDTO getDashboardStatistics(String userId) {
        if (userId == null || userId.isBlank()) {
            logger.warn("Dashboard statistics request rejected because user id is missing");
            return new DashboardResponseDTO(0.0, 0, 0, 0, 0, Collections.emptyList(), null);
        }

        DashboardResponseDTO scalars = statisticRepository.getUserScalarStatistics(userId);
        List<AuctionCardDTO> activeRooms = statisticRepository.getMyAuctionCard(userId);
        int joinedRooms = activeRooms.size();
        logger.info("Dashboard statistics loaded userId={} joinedRooms={}", userId, joinedRooms);

        return new DashboardResponseDTO(
                scalars.getBalance(),
                joinedRooms,
                scalars.getWinningBids(),
                scalars.getOutBids(),
                scalars.getWonTotal(),
                activeRooms,
                scalars.getUsername()
        );
    }

    @Override
    public AuctionDetailResponseDTO getAuctionDetail(String userId, String auctionId) {
        if (auctionId == null || auctionId.isBlank()) {
            logger.warn("Auction detail request rejected because auction id is missing userId={}", userId);
            return null;
        }
        AuctionDetailResponseDTO detail = statisticRepository.getAuctionDetail(auctionId);
        logger.info("Auction detail loaded userId={} auctionId={} found={}", userId, auctionId, detail != null);
        return detail;
    }

    @Override
    public AuctionCardDTO getJoiningAuctionCard(String auctionId) {
        if (auctionId == null || auctionId.isBlank()) {
            logger.warn("Joining auction card request rejected because auction id is missing");
            return null;
        }
        AuctionCardDTO card = statisticRepository.getJoiningAuctionCard(auctionId);
        logger.debug("Joining auction card loaded auctionId={} found={}", auctionId, card != null);
        return card;
    }

    @Override
    public List<AuctionCardDTO> getPublicAuctionCard(int quantity) {
        if (quantity <= 0) {
            logger.warn("Public auction card request rejected due to invalid quantity quantity={}", quantity);
            return Collections.emptyList();
        }
        List<AuctionCardDTO> cards = statisticRepository.getPublicAuctionCard(quantity);
        logger.info("Public auction cards loaded requested={} returned={}", quantity, cards.size());
        return cards;
    }

    @Override
    public List<AuctionCardDTO> getPostedAuctionCard(String userId, int amount) {
        if (userId == null || userId.isBlank() || amount <= 0) {
            logger.warn("Posted auction card request rejected userIdPresent={} amount={}",
                    userId != null && !userId.isBlank(), amount);
            return Collections.emptyList();
        }
        List<AuctionCardDTO> cards = statisticRepository.getPostedAuctionCard(userId, amount);
        logger.info("Posted auction cards loaded userId={} requested={} returned={}", userId, amount, cards.size());
        return cards;
    }

    @Override
    public List<AuctionCardDTO> getWonAuction(String userId, int amount) {
        if (userId == null || userId.isBlank() || amount <= 0) {
            logger.warn("Won auction request rejected userIdPresent={} amount={}",
                    userId != null && !userId.isBlank(), amount);
            return Collections.emptyList();
        }
        List<AuctionCardDTO> cards = statisticRepository.getWonAuction(userId, amount);
        logger.info("Won auctions loaded userId={} requested={} returned={}", userId, amount, cards.size());
        return cards;
    }

    @Override
    public List<AuctionCardDTO> getEndedPostedAuction(String userId, int amount) {
        if (userId == null || userId.isBlank() || amount <= 0) {
            logger.warn("Ended posted auction request rejected userIdPresent={} amount={}",
                    userId != null && !userId.isBlank(), amount);
            return Collections.emptyList();
        }
        List<AuctionCardDTO> cards = statisticRepository.getEndedPostedAuction(userId, amount);
        logger.info("Ended posted auctions loaded userId={} requested={} returned={}", userId, amount, cards.size());
        return cards;
    }
}
