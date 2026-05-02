package vn.io.huangnosimp.dto.request;

public class GetJoiningAuctionCardDTO {
    private final String auctionId;
    public GetJoiningAuctionCardDTO(String auctionId) {
        this.auctionId = auctionId;
    }
    public String getAuctionId() {
        return auctionId;
    }
}
