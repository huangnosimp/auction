package vn.io.huangnosimp.dto.request;

public class CancelAuctionRequestDTO {
    private final String auctionId;

    public CancelAuctionRequestDTO(String auctionId) {
        this.auctionId = auctionId;
    }

    public String getAuctionId() {
        return auctionId;
    }
}
