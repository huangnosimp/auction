package vn.io.huangnosimp.model;

import java.time.LocalDateTime;

public class BidTransaction extends Entity {

    private Auction auction;

    private Bidder bidder;

    private double amount;

    private LocalDateTime timestamp;

    private boolean isAutoBid;

    public BidTransaction() {
        super();
    }

    public BidTransaction(Auction auction, Bidder bidder, double amount,
                          LocalDateTime timestamp, boolean isAutoBid) {
        super();
        this.auction = auction;
        this.bidder = bidder;
        this.amount = amount;
        this.timestamp = timestamp;
        this.isAutoBid = isAutoBid;
    }


    public Auction getAuction() {
        return auction;
    }

    public void setAuction(Auction auction) {
        this.auction = auction;
    }

    public Bidder getBidder() {
        return bidder;
    }

    public void setBidder(Bidder bidder) {
        this.bidder = bidder;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isAutoBid() {
        return isAutoBid;
    }

    public void setAutoBid(boolean autoBid) {
        isAutoBid = autoBid;
    }

    @Override
    public String toString() {
        return "BidTransaction{bidder=" + (bidder != null ? bidder.getUsername() : "null") +
                ", amount=" + amount +
                ", autoBid=" + isAutoBid + "}";
    }
}
