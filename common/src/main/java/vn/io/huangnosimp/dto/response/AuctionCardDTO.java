package vn.io.huangnosimp.dto.response;

public class AuctionCardDTO {
    private String auctionId;
    private String productName;
    private double currentPrice;
    private double yourBid;
    private long endTime;

    public AuctionCardDTO(
            String auctionId,
            String productName,
            double currentPrice,
            double yourBid,
            long endTime) {
        this.auctionId = auctionId;
        this.productName = productName;
        this.currentPrice = currentPrice;
        this.yourBid = yourBid;
        this.endTime = endTime;
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

}