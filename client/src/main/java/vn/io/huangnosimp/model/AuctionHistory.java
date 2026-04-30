package vn.io.huangnosimp.model;

public class AuctionHistory {
    private String id;
    private String itemName;
    private String myBid;
    private String status;
    private String endTime;

    public AuctionHistory(String id, String itemName, String myBid, String status, String endTime) {
        this.id = id;
        this.itemName = itemName;
        this.myBid = myBid;
        this.status = status;
        this.endTime = endTime;
    }

    // Getters
    public String getId() { return id; }
    public String getItemName() { return itemName; }
    public String getMyBid() { return myBid; }
    public String getStatus() { return status; }
    public String getEndTime() { return endTime; }
}
