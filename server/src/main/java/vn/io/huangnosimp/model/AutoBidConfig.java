package vn.io.huangnosimp.model;

import java.time.LocalDateTime;

public class AutoBidConfig extends Entity {

    private Member bidder;

    private Auction auction;

    private double maxBid;

    private double increment;

    private LocalDateTime registeredAt;

    public AutoBidConfig() {
        super();
    }

    public AutoBidConfig(Member bidder, Auction auction, double maxBid,
                         double increment, LocalDateTime registeredAt) {
        super();
        this.bidder = bidder;
        this.auction = auction;
        this.maxBid = maxBid;
        this.increment = increment;
        this.registeredAt = registeredAt;
    }


    public Member getBidder() {
        return bidder;
    }

    public void setBidder(Member bidder) {
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
}
