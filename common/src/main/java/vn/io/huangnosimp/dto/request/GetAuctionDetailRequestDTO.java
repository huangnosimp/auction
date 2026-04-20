package vn.io.huangnosimp.dto.request;

public class GetAuctionDetailRequestDTO {
    private final String auctionId;
    public GetAuctionDetailRequestDTO(String auctionId) {
        this.auctionId = auctionId;
    }

    public String getAuctionId() {
        return auctionId;
    }
}
