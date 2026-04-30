package vn.io.huangnosimp.dto.response;

public class AuctionCardDTO {
    private String auctionId;
    private String productName;
    private double currentPrice;
    private double yourBid;
    private long startTime;
    private long endTime;
    private int bidCount;
    private int bidderCount;

    public AuctionCardDTO(
            String auctionId,
            String productName,
            double currentPrice,
            double yourBid,
            long startTime,
            long endTime,
            int bidCount,
            int bidderCount) {
        this.auctionId = auctionId;
        this.productName = productName;
        this.currentPrice = currentPrice;
        this.yourBid = yourBid;
        this.startTime = startTime;
        this.endTime = endTime;
        this.bidCount = bidCount;
        this.bidderCount = bidderCount;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getProductName() {
        return productName;
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

    public int getBidCount() {
        return bidCount;
    }

    public int getBidderCount() {
        return bidderCount;
    }
}
