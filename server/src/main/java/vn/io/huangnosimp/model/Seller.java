package vn.io.huangnosimp.model;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Seller extends User {

    private Set<String> postedAuctions;
    private double accountBalance;
    private boolean isBanned = false;

    public Seller(String username, String password, String email) {
        super(username, password, email);
        this.postedAuctions = ConcurrentHashMap.newKeySet();
    }

    public void addAuction(String auctionId) {
        this.postedAuctions.add(auctionId);
    }

    public void removeAuction(String auctionId) {
        this.postedAuctions.remove(auctionId);
    }

    public Set<String> getPostedAuctions() {
        return postedAuctions;
    }

    public double getAccountBalance() {
        return accountBalance;
    }

    public boolean isBanned() {
        return isBanned;
    }

    public void setBanned() {
        isBanned = true;
    }

    public void receivePayment(double amount) {
        if (amount > 0) {
            this.accountBalance += amount;
        }
    }

    public boolean withdraw(double amount) {
        if (amount > this.accountBalance || amount < 0) {
            return false;
        }
        this.accountBalance -= amount;
        return true;
    }
}
