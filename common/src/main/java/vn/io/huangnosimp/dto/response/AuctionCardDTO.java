package vn.io.huangnosimp.dto.response;

public class AuctionCardDTO {
    private String auctionId;
    private String itemName;
    private double currentPrice;
    private double yourBid;
    private long endTime;
    private long startTime;

    public AuctionCardDTO(
            String auctionId,
            String productName,
            double currentPrice,
            double yourBid,
            long endTime,
            long startTime) {
        this.auctionId = auctionId;
        this.itemName = productName;
        this.currentPrice = currentPrice;
        this.yourBid = yourBid;
        this.endTime = endTime;
        this.startTime = startTime;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getItemName() {
        return itemName;
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

    public long getStartTime(){ return startTime;}

}