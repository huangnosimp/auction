package vn.io.huangnosimp.model;

public class Auction extends Entity {

    private String itemId;

    private String sellerId;

    private String currentWinnerId;

    private double currentPrice;

    private double startPrice;

    private long startTime;

    private long endTime;

    private AuctionStatus status;

    public Auction() {
        super();
        this.status = AuctionStatus.OPEN;
    }

    public Auction(String itemId, String sellerId, double startPrice,
                   long startTime, long endTime) {
        super();
        this.itemId = itemId;
        this.sellerId = sellerId;
        this.startPrice = startPrice;
        currentPrice = startPrice;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = AuctionStatus.OPEN;
    }
    public Auction(String itemId, String sellerId, double startPrice, int durationInMinutes) {
        super();
        this.itemId = itemId;
        this.sellerId = sellerId;
        this.startPrice = startPrice;
        currentPrice = startPrice;
        this.startTime = System.currentTimeMillis();
        this.endTime = this.startTime + ((long) durationInMinutes * 60 *1000);
        this.status = AuctionStatus.OPEN;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getCurrentWinnerId() {
        return currentWinnerId;
    }

    public void setCurrentWinnerId(String currentWinnerId) {
        this.currentWinnerId = currentWinnerId;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public double getStartPrice() {
        return startPrice;
    }

    public void setStartPrice(double startPrice) {
        this.startPrice = startPrice;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
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
        return "Auction{" + "id='" + getId() + '\'' + ", itemId='" + itemId + '\'' + ", status=" + status + '\'' +", currentPrice=" + currentPrice + '}';
    }
}
