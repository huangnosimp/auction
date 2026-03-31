package vn.io.huangnosimp.model;

public class Bidder extends User {

    private double accountBalance;

    public Bidder() {
        super();
    }

    public Bidder(String username, String password, String email, double accountBalance) {
        super(username, password, email);
        this.accountBalance = accountBalance;
    }


    public double getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(double accountBalance) {
        this.accountBalance = accountBalance;
    }

    @Override
    public String toString() {
        return "Bidder{username='" + getUsername() + "', balance=" + accountBalance + "}";
    }
}
