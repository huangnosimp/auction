package vn.io.huangnosimp.repository;

import vn.io.huangnosimp.model.AutoBidConfig;

import java.util.List;

public interface IAutoBidRepository {
    void save(AutoBidConfig config);
    List<AutoBidConfig> findByAuctionId(String auctionId);
    AutoBidConfig findByMemberAndAuction(String memberId, String auctionId);
    void delete(String memberId, String auctionId);
}
