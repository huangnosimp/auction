package vn.io.huangnosimp.database;

import vn.io.huangnosimp.model.BidTransaction;

import java.util.List;

public interface BidDAO {
    void save(BidTransaction transaction);
    List<BidTransaction> findByAuctionId(String auctionId);
}
