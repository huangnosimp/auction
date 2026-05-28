package vn.io.huangnosimp.dto.response;

import vn.io.huangnosimp.enums.ItemType;

import java.util.List;

public class AuctionCardDTO {
    private String auctionId;
    private String productName;
    private ItemType itemType;
    private double currentPrice;
    private double yourBid;
    private long startTime;
    private long endTime;
    private int bidCount;
    private int bidderCount;
    private List<String> imageUrl;

    public AuctionCardDTO(
            String auctionId,
            String productName,
            double currentPrice,
            double yourBid,
            long startTime,
            long endTime,
            int bidCount,
            int bidderCount,
            List<String> imageUrl) {
        this(auctionId, productName, null, currentPrice, yourBid, startTime, endTime, bidCount, bidderCount, imageUrl);
    }

    public AuctionCardDTO(
            String auctionId,
            String productName,
            ItemType itemType,
            double currentPrice,
            double yourBid,
            long startTime,
            long endTime,
            int bidCount,
            int bidderCount,
            List<String> imageUrl) {
        this.auctionId = auctionId;
        this.productName = productName;
        this.itemType = itemType;
        this.currentPrice = currentPrice;
        this.yourBid = yourBid;
        this.startTime = startTime;
        this.endTime = endTime;
        this.bidCount = bidCount;
        this.bidderCount = bidderCount;
        this.imageUrl = imageUrl;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getProductName() {
        return productName;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public double getYourBid() {
        return yourBid;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getBidCount() {
        return bidCount;
    }

    public int getBidderCount() {
        return bidderCount;
    }

    public List<String> getImageUrl() {
        return imageUrl;
    }
}
