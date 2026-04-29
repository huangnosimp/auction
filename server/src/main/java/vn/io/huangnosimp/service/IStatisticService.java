package vn.io.huangnosimp.service;

import vn.io.huangnosimp.dto.response.AuctionCardDTO;
import vn.io.huangnosimp.dto.response.AuctionDetailResponseDTO;
import vn.io.huangnosimp.dto.response.DashboardResponseDTO;

public interface IStatisticService {
    DashboardResponseDTO getDashboardStatistics(String userId);
    AuctionDetailResponseDTO getAuctionDetail(String userId, String auctionId);
    AuctionCardDTO getAuctionCard(String userId);
}
