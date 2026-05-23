package vn.io.huangnosimp.dto.response;


public class AuctionAdminDTO {
    private String id;
    private String itemName;
    private String sellerName;
    private double startingPrice;
    private double currentPrice;
    private String startTime;
    private String endTime;
    private String status;
    private String winnerName;

    public AuctionAdminDTO(String id, String itemName, String sellerName, double startingPrice,
                           double currentPrice, String startTime, String endTime,
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

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getStatus() {
        return status;
    }

    public String getWinnerName() {
        return winnerName;
    }
}