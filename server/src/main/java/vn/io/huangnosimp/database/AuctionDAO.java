package vn.io.huangnosimp.database;

import vn.io.huangnosimp.model.Auction;

public interface AuctionDAO {
    void save(Auction auction);
    void update(Auction auction);
    Auction findById(String id);
    void deleteById(String id);
}
