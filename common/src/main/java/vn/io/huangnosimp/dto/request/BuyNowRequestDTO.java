package vn.io.huangnosimp.dto.request;

public class BuyNowRequestDTO {
    private final String auctionId;
    public BuyNowRequestDTO(String auctionId) {
        this.auctionId = auctionId;
    }

    public String getAuctionId() {
        return auctionId;
    }
}
