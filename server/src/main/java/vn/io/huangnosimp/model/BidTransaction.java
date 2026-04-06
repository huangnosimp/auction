package vn.io.huangnosimp.model;

public class BidTransaction extends Entity {

    private String auctionId;

    private String bidderId;

    private double amount;

    private boolean isAutoBid;

    public BidTransaction(String auctionId, String bidderId, double amount, boolean isAutoBid) {
        super();
        super.setCreatedAt(System.currentTimeMillis());
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.amount = amount;
        this.isAutoBid = isAutoBid;
    }

    //constructor cho DAO
    public BidTransaction(String Id, String auctionId, String bidderId, double amount, long createdAt, boolean isAutoBid) {
        super(Id);
        super.setCreatedAt(createdAt);
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.amount = amount;
        this.isAutoBid = isAutoBid;
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

    public boolean isAutoBid() {
        return isAutoBid;
    }

    public void setAutoBid(boolean autoBid) {
        isAutoBid = autoBid;
    }

    @Override
    public String toString() {
        return "Auction{" + "id='" + getId() + '\'' + ", auctionId='" + auctionId + '\'' + ", bidderId=" + bidderId + '\'' + ", amount=" + amount + '\'' + ", createdAt=" + super.getCreatedAt() + '}';
    }
}
