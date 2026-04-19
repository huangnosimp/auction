package vn.io.huangnosimp.dto.response;

import vn.io.huangnosimp.enums.AuctionStatus;

public class AuctionResponseDTO {
    private final int totalBidder;
    private final String itemName;
    private final String description;
    private final String auctionId;
    private final String currentWinnerUserName;
    private final String sellerUserName;
    private final double currentPrice;
    private final double startPrice;
    private final long startTime;
    private final long endTime;
    private final AuctionStatus auctionStatus;

    public AuctionResponseDTO(
            int totalBidder,
            String itemName,
            String description,
            String auctionId,
            String currentWinnerUserName,
            String sellerUserName,
            double currentPrice,
            double startPrice,
            long startTime,
            long endTime,
            AuctionStatus auctionStatus) {
        this.totalBidder = totalBidder;
        this.itemName = itemName;
        this.description = description;
        this.auctionId = auctionId;
        this.currentWinnerUserName = currentWinnerUserName;
        this.sellerUserName = sellerUserName;
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

    public String getItemName() {
        return itemName;
    }

    public String getDescription() {
        return description;
    }

    public AuctionStatus getAuctionStatus() {
        return auctionStatus;
    }
}
