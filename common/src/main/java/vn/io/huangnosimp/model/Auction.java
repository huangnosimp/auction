package vn.io.huangnosimp.model;

import java.util.concurrent.ConcurrentHashMap;

public class Auction extends Entity {
    private final ConcurrentHashMap<String, Bidder> bidders;
    private final Item item;

    private final Seller seller;

    private String currentWinnerId;

    private double currentPrice;

    private final double startPrice;

    private final long startTime;

    private long endTime;

    private volatile AuctionStatus status;


    public Auction(Item item, Seller seller, double startPrice,
                   long startTime, long endTime) {
        super();
        this.item = item;
        this.seller = seller;
        this.startPrice = startPrice;
        currentPrice = startPrice;
        this.startTime = startTime;
        this.endTime = endTime;
        bidders = new ConcurrentHashMap<>();
    }
    public Auction(Item item, Seller seller, double startPrice, int durationInMinutes) {
        super();
        this.item = item;
        this.seller = seller;
        this.startPrice = startPrice;
        currentPrice = startPrice;
        this.startTime = System.currentTimeMillis();
        this.endTime = this.startTime + ((long) durationInMinutes * 60 *1000);
        bidders = new ConcurrentHashMap<>();
    }

    public Item getItem() {
        return item;
    }

    public Seller getSeller() {
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

    public ConcurrentHashMap<String, Bidder> getBidders() {
        return bidders;
    }

    public long getEndTime() {
        return endTime;
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

    public Bidder getBidder(String bidderId) {
        return this.bidders.get(bidderId);
    }

    public void addBidder(Bidder bidder) {
        if (bidder == null || bidder.getId() == null) {
            return;
        }
        this.bidders.put(bidder.getId(), bidder);
    }

    public void removeBidder(String bidderId) {
        this.bidders.remove(bidderId);
    }

    public synchronized boolean placeBid(Bidder bidder, double amount) {
        if (bidder == null || this.status != AuctionStatus.RUNNING) {
            return false;
        }
        if (amount <= this.currentPrice) {
            return false;
        }

        String previousWinnerId = this.currentWinnerId;
        double previousPrice = this.currentPrice;

        // Same winner only needs to freeze the delta between old and new bid.
        if (previousWinnerId != null && previousWinnerId.equals(bidder.getId())) {
            double delta = amount - previousPrice;
            if (!bidder.freezeMoney(delta)) {
                return false;
            }
            this.addBidder(bidder);
            this.currentPrice = amount;
            return true;
        }

        if (!bidder.freezeMoney(amount)) {
            return false;
        }

        this.addBidder(bidder);
        if (previousWinnerId != null) {
            Bidder previousWinner = this.bidders.get(previousWinnerId);
            if (previousWinner != null && !previousWinner.unfreezeMoney(previousPrice)) {
                bidder.unfreezeMoney(amount);
                return false;
            }
        }

        this.currentWinnerId = bidder.getId();
        this.currentPrice = amount;
        return true;
    }

    public boolean needExtension() {
        long timeLeft = this.endTime - System.currentTimeMillis();
        return timeLeft <= 10 * 1000;
    }
}
