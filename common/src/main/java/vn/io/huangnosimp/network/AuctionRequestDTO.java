package vn.io.huangnosimp.network;

public class AuctionRequestDTO {
    private String auctionId;
    private double startPrice;
    private long startTime;
    private long endTime;
    private int durationInMinutes;
    private double amount;

    public AuctionRequestDTO(String auctionId, double startPrice, long startTime, long endTime, int durationInMinutes) {
        this.auctionId = auctionId;
        this.startPrice = startPrice;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationInMinutes = durationInMinutes;
    }

    public String getAuctionId() {
        return auctionId;
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

    public int getDurationInMinutes() {
        return durationInMinutes;
    }

    public double getAmount() {
        return amount;
    }
}
