package vn.io.huangnosimp.dto.request;

public class LeaveRoomRequestDTO {
    private final String auctionId;

    public LeaveRoomRequestDTO(String auctionId) {
        this.auctionId = auctionId;
    }

    public String getAuctionId() {
        return auctionId;
    }
}
