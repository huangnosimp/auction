package vn.io.huangnosimp.service;

import vn.io.huangnosimp.dto.response.DashboardResponseDTO;
import vn.io.huangnosimp.repository.*;

public class StatisticService implements IStatisticService {
    private final IStatisticRepository statisticRepository;

    public StatisticService(IStatisticRepository statisticRepository) {
        this.statisticRepository = statisticRepository;
    }

    @Override
    public DashboardResponseDTO getDashboardStatistics(String userId) {
        double amount = statisticRepository.getAccountBalance(userId);
        int joinedRooms = statisticRepository.getJoinedActiveRoomsCount(userId);
        int winningBids = statisticRepository.getWinningBidsCount(userId);
        int outBids = statisticRepository.getOutBidsCount(userId);
        int wonTotal = statisticRepository.getWonTotalCount(userId);
        return new DashboardResponseDTO(amount, joinedRooms, winningBids, outBids, wonTotal, null);
    }
}
