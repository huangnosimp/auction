package vn.io.huangnosimp.dto.response;

public class PricePointDTO {
    private final long timestamp;   // Thời điểm (trục X của LineChart)
    private final double price;     // Giá tại thời điểm đó (trục Y của LineChart)

    public PricePointDTO(long timestamp, double price) {
        this.timestamp = timestamp;
        this.price = price;
    }

    public long getTimestamp() { return timestamp; }
    public double getPrice() { return price; }
}