package vn.io.huangnosimp.database;

import vn.io.huangnosimp.model.AutoBidConfig;

import java.util.List;

public interface AutoBidDAO {
    void save(AutoBidConfig config);
    void update(AutoBidConfig config);
    List<AutoBidConfig> findByAuctionId(String auctionId);
}
