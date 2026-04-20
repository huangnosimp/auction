package vn.io.huangnosimp.dto.request;

public class DepositWithdrawRequestDTO {
    private final double amount;
    public DepositWithdrawRequestDTO(double amount) {
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }
}
