package vn.io.huangnosimp.model;

import vn.io.huangnosimp.dto.response.AuctionDetailResponseDTO;
import vn.io.huangnosimp.enums.AuctionStatus;

public class Auction extends Entity {
    private final Item item;
    private final Member seller;
    private String currentWinnerId;
    private double currentPrice;
    private final double startPrice;
    private final long startTime;
    private long endTime;
    private volatile AuctionStatus status;
    private double minimumIncrement;
    private double buyNowPrice;

    public Auction(
            Item item,
            Member seller,
            double startPrice,
            long startTime,
            long endTime,
            double minBid,
            double buyNowPrice) {
        super();
        this.item = item;
        this.seller = seller;
        this.startPrice = startPrice;
        currentPrice = startPrice;
        this.startTime = startTime;
        this.endTime = endTime;
        this.minimumIncrement = minBid;
        this.buyNowPrice = buyNowPrice;
    }

    public Auction(
            String id,
            Item item,
            Member seller,
            double startPrice,
            long startTime,
            long endTime, long createdAt) {
        super(id, createdAt);
        this.item = item;
        this.seller = seller;
        this.startPrice = startPrice;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Item getItem() {
        return item;
    }

    public Member getSeller() {
        return seller;
    }

    public String getCurrentWinnerId() {
        return currentWinnerId;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public double getStartPrice() {
        return startPrice;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setCurrentWinnerId(String currentWinnerId) {
        this.currentWinnerId = currentWinnerId;
    }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public boolean extendEndTime(long newEndTime) {
        if (newEndTime <= this.endTime) {
            return false;
        }
        this.endTime = newEndTime;
        return true;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public void setStatusOPEN() {
        this.status = AuctionStatus.OPEN;
    }

    public void setStatusRunning() {
        if (this.status == null || this.status == AuctionStatus.OPEN) {
            this.status = AuctionStatus.RUNNING;
        }
    }

    public void setStatusFinish() {
        if (this.status == AuctionStatus.RUNNING) {
            this.status = AuctionStatus.FINISHED;
        }
    }

    public void setStatusPaid() {
        if (this.status == AuctionStatus.FINISHED) {
            this.status = AuctionStatus.PAID;
        }
    }

    public void setStatusCanceled() {
        this.status = AuctionStatus.CANCELED;
    }

    public boolean needExtension() {
        long timeLeft = this.endTime - System.currentTimeMillis();
        return timeLeft <= 10 * 1000;
    }

    public double getMinimumIncrement() {
        return minimumIncrement;
    }

    public void setMinimumIncrement(double minimumIncrement) {
        this.minimumIncrement = minimumIncrement;
    }

    public double getBuyNowPrice() {
        return buyNowPrice;
    }

    public void setBuyNowPrice(double buyNowPrice) {
        this.buyNowPrice = buyNowPrice;
    }
}
