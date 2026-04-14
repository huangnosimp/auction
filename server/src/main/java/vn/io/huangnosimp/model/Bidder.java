package vn.io.huangnosimp.model;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Bidder extends User {

    private double accountBalance;
    private double frozenBalance = 0.0;
    private final Set<String> joinedAuctions;
    private boolean isBanned = false;

    public Bidder(String username, String password, String email) {
        super(username, password, email);
        this.joinedAuctions = ConcurrentHashMap.newKeySet();
    }


    public double getAccountBalance() {
        return accountBalance;
    }

    public double getFrozenBalance() {
        return frozenBalance;
    }

    public boolean isBanned() {
        return isBanned;
    }

    public void setBanned() {
        isBanned = true;
    }

    public synchronized boolean freezeMoney(double amount) {
        if (amount <= 0 || amount > this.accountBalance) {
            return false;
        }
        this.accountBalance -= amount;
        this.frozenBalance += amount;
        return true;
    }

    public synchronized boolean unfreezeMoney(double amount) {
        if (amount <= 0 || amount > this.frozenBalance) {
            return false;
        }
        this.accountBalance += amount;
        this.frozenBalance -= amount;
        return true;
    }

    public synchronized boolean deductFrozenMoney(double amount) {
        if (amount <= 0 || amount > this.frozenBalance) {
            return false;
        }
        this.frozenBalance -= amount;
        return true;
    }

    public void joinRoom(String auctionId) {
        this.joinedAuctions.add(auctionId);
    }

    public void leaveRoom(String auctionId) {
        this.joinedAuctions.remove(auctionId);
    }

    public Set<String> getJoinedAuctions() {
        return this.joinedAuctions;
    }

    public boolean deposit(double amount) {
        if (amount <= 0) {
            return false;
        }
        this.accountBalance += amount;
        return true;
    }

    @Override
    public String toString() {
        return "Bidder{username='" + getUsername() + "', balance=" + accountBalance + "}";
    }
}
