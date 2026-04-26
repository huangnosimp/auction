package vn.io.huangnosimp.dto.response;

import vn.io.huangnosimp.enums.ItemCondition;
import vn.io.huangnosimp.enums.ItemType;

import java.util.List;

public class AuctionDetailResponseDTO {


    // Thông tin sản phẩm
    private final String productName;      // Tên sản phẩm
    private final ItemType category;         // Danh mục (Electronics, Art, Vehicle...)
    private final ItemCondition condition;        // Tình trạng (New, Like New, Used)
    private final String description;      // Mô tả chi tiết sản phẩm

    // Định giá
    private final double startPrice;       // Giá khởi điểm
    private final double bidIncrement;     // Bước giá tối thiểu mỗi lần đặt
    private final double buyNowPrice;      // Giá mua ngay (0 nếu không có)

    // Lịch trình
    private final long startTime;          // Thời điểm bắt đầu (epoch milliseconds)
    private final long endTime;            // Thời điểm kết thúc (epoch milliseconds)

    // Trạng thái hiện tại
    private final double currentPrice;     // Giá hiện tại cao nhất
    private final double minNextBid;       // Giá tối thiểu cho lượt đặt tiếp theo
    private final String leadBidder;       // Người đang dẫn đầu
    private final long lastBidTime;        // Thời điểm đặt giá gần nhất (epoch milliseconds)
    private final int participantCount;    // Số người đang tham gia phiên
    private final int bidCount;            // Tổng số lượt đặt giá

    // Lịch sử
    private final List<BidHistoryDTO> bidHistory;   // Danh sách lịch sử đặt giá (hiển thị ListView)
    private final List<PricePointDTO> priceHistory; // Danh sách điểm giá (vẽ LineChart)

    public AuctionDetailResponseDTO(String productName, ItemType category, ItemCondition condition, String description,
                                    double startPrice, double bidIncrement, double buyNowPrice, double reservePrice,
                                    long startTime, long endTime,
                                    double currentPrice, double minNextBid,
                                    String leadBidder, long lastBidTime,
                                    int participantCount, int bidCount,
                                    List<BidHistoryDTO> bidHistory, List<PricePointDTO> priceHistory) {
        this.productName = productName;
        this.category = category;
        this.condition = condition;
        this.description = description;
        this.startPrice = startPrice;
        this.bidIncrement = bidIncrement;
        this.buyNowPrice = buyNowPrice;
        this.startTime = startTime;
        this.endTime = endTime;
        this.currentPrice = currentPrice;
        this.minNextBid = minNextBid;
        this.leadBidder = leadBidder;
        this.lastBidTime = lastBidTime;
        this.participantCount = participantCount;
        this.bidCount = bidCount;
        this.bidHistory = bidHistory;
        this.priceHistory = priceHistory;
    }

    public String getProductName() { return productName; }

    public ItemType getCategory() {
        return category;
    }

    public ItemCondition getCondition() {
        return condition;
    }

    public String getDescription() { return description; }
    public double getStartPrice() { return startPrice; }
    public double getBidIncrement() { return bidIncrement; }
    public double getBuyNowPrice() { return buyNowPrice; }
    public long getStartTime() { return startTime; }
    public long getEndTime() { return endTime; }
    public double getCurrentPrice() { return currentPrice; }
    public double getMinNextBid() { return minNextBid; }
    public String getLeadBidder() { return leadBidder; }
    public long getLastBidTime() { return lastBidTime; }
    public int getParticipantCount() { return participantCount; }
    public int getBidCount() { return bidCount; }
    public List<BidHistoryDTO> getBidHistory() { return bidHistory; }
    public List<PricePointDTO> getPriceHistory() { return priceHistory; }
}
