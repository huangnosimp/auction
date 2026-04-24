package vn.io.huangnosimp.model;

public class BidTransaction extends Entity {

    private String auctionId;
    private String bidderId;
    private double amount;

    public BidTransaction(String auctionId, String bidderId, double amount) {
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.amount = amount;
    }

    public BidTransaction(String id, String auctionId, String bidderId, double amount, long createdAt) {
        super(id, createdAt);
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.amount = amount;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }

    public String getBidderId() {
        return bidderId;
    }

    public void setBidderId(String bidderId) {
        this.bidderId = bidderId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

}
