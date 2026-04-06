package vn.io.huangnosimp.network;

import vn.io.huangnosimp.model.Auction;
import vn.io.huangnosimp.model.AuctionStatus;


public class AuctionDTO {
    private int totalBidder;
    private String currentWinnerUserName;
    private String sellerUserName;
    private double currentPrice;
    private double startPrice;
    private long startTime;
    private long endTime;
    private AuctionStatus status;

    public AuctionDTO(Auction auction, String currentWinnerUserName) {
        this.sellerUserName = auction.getSeller().getUsername();
        this.currentWinnerUserName = currentWinnerUserName;
        this.totalBidder = auction.getBidders().size();
        this.currentPrice = auction.getCurrentPrice();
        this.startPrice = auction.getStartPrice();
        this.startTime = auction.getStartTime();
        this.endTime = auction.getEndTime();
        this.status = auction.getStatus();
    }

    public int getTotalBidder() {
        return totalBidder;
    }

    public String getCurrentWinnerUserName() {
        return currentWinnerUserName;
    }

    public String getSellerUserName() {
        return sellerUserName;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public double getStartPrice() {
        return startPrice;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public AuctionStatus getStatus() {
        return status;
    }
}
