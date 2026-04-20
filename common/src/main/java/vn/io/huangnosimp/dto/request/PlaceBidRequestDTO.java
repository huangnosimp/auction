package vn.io.huangnosimp.dto.request;

public class PlaceBidRequestDTO {
    private final String auctionId;
    private final double bidAmount;
    public PlaceBidRequestDTO(String auctionId, double bidAmount) {
        this.auctionId = auctionId;
        this.bidAmount = bidAmount;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public double getBidAmount() {
        return bidAmount;
    }
}
