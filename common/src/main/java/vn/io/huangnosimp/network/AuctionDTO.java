package vn.io.huangnosimp.network;

import vn.io.huangnosimp.model.AuctionStatus;


public class AuctionDTO {
    private int totalBidder;
    private String auctionId;
    private String currentWinnerUserName;
    private String sellerUserName;
    private double currentPrice;
    private double startPrice;
    private long startTime;
    private long endTime;
    private AuctionStatus auctionStatus;

    public AuctionDTO(String auctionId, String sellerUserName, String currentWinnerUserName, int totalBidder, double currentPrice, double startPrice, long startTime, long endTime, AuctionStatus auctionStatus) {
        this.auctionId = auctionId;
        this.sellerUserName = sellerUserName;
        this.currentWinnerUserName = currentWinnerUserName;
        this.totalBidder = totalBidder;
        this.currentPrice = currentPrice;
        this.startPrice = startPrice;
        this.startTime = startTime;
        this.endTime = endTime;
        this.auctionStatus = auctionStatus;
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

    public String getAuctionId() {
        return auctionId;
    }

    public AuctionStatus getStatus() {
        return auctionStatus;
    }
}
