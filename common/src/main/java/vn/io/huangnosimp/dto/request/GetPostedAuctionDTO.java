package vn.io.huangnosimp.dto.request;

public class GetPostedAuctionDTO {
    private final int amount;
    public GetPostedAuctionDTO(int amount) {
        this.amount = amount;
    }
    public int getAmount() {
        return amount;
    }
}
