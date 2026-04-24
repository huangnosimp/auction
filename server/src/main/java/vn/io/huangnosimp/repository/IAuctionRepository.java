package vn.io.huangnosimp.repository;

import vn.io.huangnosimp.model.Auction;

public interface IAuctionRepository {
    void save(Auction auction);
    Auction findById(String auctionId);
    boolean delete(String auctionId);
}
