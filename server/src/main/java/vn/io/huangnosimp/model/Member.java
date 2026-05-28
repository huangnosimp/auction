package vn.io.huangnosimp.model;

import java.time.LocalDateTime;

public class Member extends User {
    protected double accountBalance;
    protected double frozenBalance;
    protected boolean isBanned;
    protected LocalDateTime banUntil;

    public Member(String id, String username, String password, String email, double accountBalance, double frozenBalance, boolean isBanned, LocalDateTime banUntil, long createdAt) {
        super(id, username, password, email, createdAt);
        this.accountBalance = accountBalance;
        this.frozenBalance = frozenBalance;
        this.isBanned = isBanned;
        this.banUntil = banUntil;
    }

    public LocalDateTime getBanUntil() {
        return banUntil;
    }

    public void setBanUntil(LocalDateTime banUntil) {
        this.banUntil = banUntil;
    }

    public synchronized double getAccountBalance() {
        return accountBalance;
    }

    public synchronized double getFrozenBalance() {
        return frozenBalance;
    }

    public synchronized void setAccountBalance(double accountBalance) {
        this.accountBalance = accountBalance;
    }

    public synchronized void setFrozenBalance(double frozenBalance) {
        this.frozenBalance = frozenBalance;
    }

    public boolean isBanned() {
        return isBanned;
    }

    public synchronized void setBanned(boolean banned) {
        this.isBanned = banned;
    }

    public synchronized boolean deposit(double amount) {
        if (amount <= 0) {
            return false;
        }
        this.accountBalance += amount;
        return true;
    }

    public synchronized boolean withdraw(double amount) {
        if (amount <= 0 || amount > this.accountBalance) {
            return false;
        }
        this.accountBalance -= amount;
        return true;
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

    public synchronized void receivePayment(double amount) {
        deposit(amount);
    }
}
