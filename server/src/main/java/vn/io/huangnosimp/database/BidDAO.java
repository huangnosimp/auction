package vn.io.huangnosimp.database;

import vn.io.huangnosimp.model.BidTransaction;

import java.util.List;

/**
 * DAO interface for BidTransaction persistence operations.
 * Implementation will be provided in the database layer.
 */
public interface BidDAO {
    void save(BidTransaction transaction);
    List<BidTransaction> findByAuctionId(String auctionId);
}
