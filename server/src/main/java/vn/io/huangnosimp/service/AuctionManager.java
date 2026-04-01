package vn.io.huangnosimp.service;

import vn.io.huangnosimp.model.Auction;

import java.util.ArrayList;
//Dùng Singleton pattern
//Đổi chức năng thành:
//- cập nhật trạng thái cho Auction
//- 
public class AuctionManager {
    private static AuctionManager instance;
    private ArrayList<Auction> auctions;

    private AuctionManager() {
        ArrayList<Auction> auctions = new ArrayList<>();
    }

    public static synchronized AuctionManager getInstance() {
        if (instance == null) {
            instance = new AuctionManager();
        }
        return instance;
    }


    public void addAuction(Auction auction) {
        if (auction != null) {
            auctions.add(auction);
        }
    }
    public void removeAuction(Auction auction) {
        if (auction != null) {
            auctions.remove(auction);
        }
    }
}
