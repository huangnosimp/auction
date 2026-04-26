package vn.io.huangnosimp.dto.response;

public class BidHistoryDTO {
    private final String bidder;    // Tên/mã người đặt giá
    private final double price;     // Giá đã đặt
    private final long timestamp;   // Thời điểm đặt (epoch milliseconds)

    public BidHistoryDTO(String bidder, double price, long timestamp) {
        this.bidder = bidder;
        this.price = price;
        this.timestamp = timestamp;
    }

    public String getBidder() { return bidder; }
    public double getPrice() { return price; }
    public long getTimestamp() { return timestamp; }
}
