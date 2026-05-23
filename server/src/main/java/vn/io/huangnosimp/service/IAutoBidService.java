package vn.io.huangnosimp.service;

import vn.io.huangnosimp.dto.response.AutoBidResponseDTO;

import java.util.List;

public interface IAutoBidService {
    boolean registerAutoBid(String bidderId, String auctionId, double maxBid, double increment);
    void processAutoBids(String auctionId);
    void unregisterAutoBid(String bidderId, String auctionId);
    List<AutoBidResponseDTO> getAutoBidsByUserId(String userId);
}
