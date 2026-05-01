package vn.io.huangnosimp.dto.request;

public class GetPublicAcutionCardDTO {
    private final int quantity;
    public GetPublicAcutionCardDTO(int quantity) {
        this.quantity = quantity;
    }
    public int getQuantity() {
        return quantity;
    }
}
