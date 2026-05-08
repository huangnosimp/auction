package vn.io.huangnosimp.repository;

import vn.io.huangnosimp.dto.response.AuctionCardDTO;
import vn.io.huangnosimp.dto.response.DashboardResponseDTO;

import java.util.List;

import vn.io.huangnosimp.dto.response.AuctionDetailResponseDTO;

public interface IStatisticRepository {
    DashboardResponseDTO getUserScalarStatistics(String userId);
    List<AuctionCardDTO> getMyAuctionCard(String userId);
    AuctionDetailResponseDTO getAuctionDetail(String auctionId);
    AuctionCardDTO getJoiningAuctionCard(String auctionId);
    List<AuctionCardDTO> getPublicAuctionCard(int quantity);
    List<AuctionCardDTO> getPostedAuctionCard(String userId);
}
