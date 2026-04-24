package vn.io.huangnosimp.repository;

import vn.io.huangnosimp.model.Auction;

public interface IAuctionRepository {
    void save(Auction auction);
    Auction findById(String auctionId);
    boolean delete(String auctionId);
    void addParticipant(String auctionId, String userId);
    void removeParticipant(String auctionId, String userId);
    boolean isParticipant(String auctionId, String userId);
    int getParticipantCount(String auctionId);
}
