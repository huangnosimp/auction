package vn.io.huangnosimp.dto.response;

import java.time.LocalDateTime;

public class AuctionAdminDTO {
    private String id;
    private String itemName;
    private String sellerName;
    private double startingPrice;
    private double currentPrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String winnerName;

    public AuctionAdminDTO(String id, String itemName, String sellerName, double startingPrice,
                           double currentPrice, LocalDateTime startTime, LocalDateTime endTime,
                           String status, String winnerName) {
        this.id = id;
        this.itemName = itemName;
        this.sellerName = sellerName;
        this.startingPrice = startingPrice;
        this.currentPrice = currentPrice;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.winnerName = winnerName;
    }

    public String getId() {
        return id;
    }

    public String getItemName() {
        return itemName;
    }

    public String getSellerName() {
        return sellerName;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getStatus() {
        return status;
    }

    public String getWinnerName() {
        return winnerName;
    }
}