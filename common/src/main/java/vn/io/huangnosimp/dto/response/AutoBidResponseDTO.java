package vn.io.huangnosimp.dto.response;

import java.time.LocalDateTime;

public class AutoBidResponseDTO {
    private String id;
    private String auctionId;
    private double maxBid;
    private double increment;
    private String registeredAt;

    public AutoBidResponseDTO(String id, double maxBid, double increment, String auctionId, String registeredAt) {
        this.id = id;
        this.maxBid = maxBid;
        this.increment = increment;
        this.auctionId = auctionId;
        this.registeredAt = registeredAt;
    }

    public String getId() {
        return id;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public double getMaxBid() {
        return maxBid;
    }

    public double getIncrement() {
        return increment;
    }

    public String getRegisteredAt() {
        return registeredAt;
    }

}