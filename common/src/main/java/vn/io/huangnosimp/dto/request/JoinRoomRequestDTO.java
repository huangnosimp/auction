package vn.io.huangnosimp.dto.request;

public class JoinRoomRequestDTO {
    private final String auctionId;

    public JoinRoomRequestDTO(String auctionId) {
        this.auctionId = auctionId;
    }

    public String getAuctionId() {
        return auctionId;
    }
}
