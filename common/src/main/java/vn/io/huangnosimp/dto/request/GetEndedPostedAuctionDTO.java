package vn.io.huangnosimp.dto.request;

public class GetEndedPostedAuctionDTO {
    private final int amount;

    public GetEndedPostedAuctionDTO(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }
}
