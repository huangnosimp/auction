package vn.io.huangnosimp.model;

import java.time.LocalDateTime;

public class Auction extends Entity {

    private Item item;

    private Seller seller;

    private Bidder currentWinner;

    private double currentPrice;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private AuctionStatus status;

    public Auction() {
        super();
        this.status = AuctionStatus.OPEN;
    }

    public Auction(Item item, Seller seller, double currentPrice,
                   LocalDateTime startTime, LocalDateTime endTime) {
        super();
        this.item = item;
        this.seller = seller;
        this.currentPrice = currentPrice;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = AuctionStatus.OPEN;
    }


    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Seller getSeller() {
        return seller;
    }

    public void setSeller(Seller seller) {
        this.seller = seller;
    }

    public Bidder getCurrentWinner() {
        return currentWinner;
    }

    public void setCurrentWinner(Bidder currentWinner) {
        this.currentWinner = currentWinner;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Auction{item=" + (item != null ? item.getName() : "null") +
                ", status=" + status +
                ", currentPrice=" + currentPrice + "}";
    }
}
