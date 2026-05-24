package vn.io.huangnosimp.repository;

import vn.io.huangnosimp.model.Auction;

import java.util.List;

public interface IAuctionRepository {
    boolean save(Auction auction);
    Auction findById(String auctionId);
    boolean delete(String auctionId);
    List<Auction> findAll();
}
