package vn.io.huangnosimp.dto.response;

public class PlaceBidResponseDTO {
    private String username;
    private double amount;
    private long placeAt;

    public PlaceBidResponseDTO(String username, double amount, long placeAt) {
        this.username = username;
        this.amount = amount;
        this.placeAt = placeAt;
    }

    public String getUsername() {
        return username;
    }

    public double getAmount() {
        return amount;
    }

    public long getPlaceAt() {
        return placeAt;
    }
}
