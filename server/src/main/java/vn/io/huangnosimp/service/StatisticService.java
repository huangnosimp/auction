package vn.io.huangnosimp.service;

import vn.io.huangnosimp.dto.response.AuctionCardDTO;
import vn.io.huangnosimp.dto.response.AuctionDetailResponseDTO;
import vn.io.huangnosimp.dto.response.DashboardResponseDTO;
import vn.io.huangnosimp.repository.*;

import java.util.List;
import java.util.Collections;

public class StatisticService implements IStatisticService {
    private final IStatisticRepository statisticRepository;

    public StatisticService(IStatisticRepository statisticRepository) {
        this.statisticRepository = statisticRepository;
    }

    @Override
    public DashboardResponseDTO getDashboardStatistics(String userId) {
        if (userId == null || userId.isBlank()) {
            return new DashboardResponseDTO(0.0, 0, 0, 0, 0, Collections.emptyList(), null);
        }

        DashboardResponseDTO scalars = statisticRepository.getUserScalarStatistics(userId);
        List<AuctionCardDTO> activeRooms = statisticRepository.getMyAuctionCard(userId);
        int joinedRooms = activeRooms.size();

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
            return null;
        }
        return statisticRepository.getAuctionDetail(auctionId);
    }

    @Override
    public AuctionCardDTO getJoiningAuctionCard(String auctionId) {
        if (auctionId == null || auctionId.isBlank()) {
            return null;
        }
        return statisticRepository.getJoiningAuctionCard(auctionId);
    }

    @Override
    public List<AuctionCardDTO> getPublicAuctionCard(int quantity) {
        if (quantity <= 0) {
            return Collections.emptyList();
        }
        return statisticRepository.getPublicAuctionCard(quantity);
    }

    @Override
    public List<AuctionCardDTO> getPostedAuctionCard(String userId) {
        if (userId == null || userId.isBlank()) {
            return Collections.emptyList();
        }
        return statisticRepository.getPostedAuctionCard(userId);
    }
}
