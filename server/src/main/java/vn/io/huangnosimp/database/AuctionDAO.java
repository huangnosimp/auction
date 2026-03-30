package vn.io.huangnosimp.database;

import vn.io.huangnosimp.model.Auction;

/**
 * DAO interface for Auction persistence operations.
 * Implementation will be provided in the database layer.
 */
public interface AuctionDAO {
    void save(Auction auction);
    void update(Auction auction);
    Auction findById(String id);
    void deleteById(String id);
}
