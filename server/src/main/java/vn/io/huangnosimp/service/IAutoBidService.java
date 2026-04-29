package vn.io.huangnosimp.service;

public interface IAutoBidService {
    boolean registerAutoBid(String bidderId, String auctionId, double maxBid, double increment);
    void processAutoBids(String auctionId);
    void unregisterAutoBid(String bidderId, String auctionId);
}
