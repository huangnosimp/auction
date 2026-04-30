package vn.io.huangnosimp.repository;

import vn.io.huangnosimp.dto.response.AuctionCardDTO;
import vn.io.huangnosimp.dto.response.DashboardResponseDTO;

import java.util.List;

import vn.io.huangnosimp.dto.response.AuctionDetailResponseDTO;

public interface IStatisticRepository {
    DashboardResponseDTO getUserScalarStatistics(String userId);
    List<AuctionCardDTO> getActiveRooms(String userId);
    AuctionDetailResponseDTO getAuctionDetail(String auctionId);
    AuctionCardDTO getAuctionCard(String auctionId);
}
