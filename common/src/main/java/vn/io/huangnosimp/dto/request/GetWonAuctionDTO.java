package vn.io.huangnosimp.dto.request;

public class GetWonAuctionDTO {
    private final int amount;
    public GetWonAuctionDTO(int amount) {
        this.amount = amount;
    }
    public int getAmount() {
        return amount;
    }
}
