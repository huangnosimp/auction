package vn.io.huangnosimp.dto.response;

public class AuctionCardDTO {
    private String auctionId;
    private String productName;
    private double currentPrice;
    private double yourBid;
    private long endTime;
    private int totalBids;

    public AuctionCardDTO(
            String auctionId,
            String productName,
            double currentPrice,
            double yourBid,
            long endTime,
            int totalBids) {
        this.auctionId = auctionId;
        this.productName = productName;
        this.currentPrice = currentPrice;
        this.yourBid = yourBid;
        this.endTime = endTime;
        this.totalBids = totalBids;
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

    public long getEndTime() {
        return endTime;
    }

    public int getTotalBids() {
        return totalBids;
    }


}