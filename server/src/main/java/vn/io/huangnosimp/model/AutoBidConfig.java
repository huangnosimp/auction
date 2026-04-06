package vn.io.huangnosimp.model;

import java.time.LocalDateTime;

public class AutoBidConfig extends Entity {

    private Bidder bidder;

    private Auction auction;

    private double maxBid;

    private double increment;

    private LocalDateTime registeredAt;

    public AutoBidConfig() {
        super();
    }

    public AutoBidConfig(Bidder bidder, Auction auction, double maxBid,
                         double increment, LocalDateTime registeredAt) {
        super();
        this.bidder = bidder;
        this.auction = auction;
        this.maxBid = maxBid;
        this.increment = increment;
        this.registeredAt = registeredAt;
    }


    public Bidder getBidder() {
        return bidder;
    }

    public void setBidder(Bidder bidder) {
        this.bidder = bidder;
    }

    public Auction getAuction() {
        return auction;
    }

    public void setAuction(Auction auction) {
        this.auction = auction;
    }

    public double getMaxBid() {
        return maxBid;
    }

    public void setMaxBid(double maxBid) {
        this.maxBid = maxBid;
    }

    public double getIncrement() {
        return increment;
    }

    public void setIncrement(double increment) {
        this.increment = increment;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    @Override
    public String toString() {
        return "AutoBidConfig{bidder=" + (bidder != null ? bidder.getUsername() : "null") +
                ", maxBid=" + maxBid +
                ", increment=" + increment + "}";
    }
}
