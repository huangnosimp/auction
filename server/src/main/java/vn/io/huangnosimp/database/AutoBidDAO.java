package vn.io.huangnosimp.database;

import vn.io.huangnosimp.model.AutoBidConfig;

import java.util.List;

/**
 * DAO interface for AutoBidConfig persistence operations.
 * Implementation will be provided in the database layer.
 */
public interface AutoBidDAO {
    void save(AutoBidConfig config);
    void update(AutoBidConfig config);
    List<AutoBidConfig> findByAuctionId(String auctionId);
}
