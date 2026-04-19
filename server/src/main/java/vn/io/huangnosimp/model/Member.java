package vn.io.huangnosimp.model;

public class Member extends User {
    protected double accountBalance;
    protected double frozenBalance = 0.0;
    protected boolean isBanned = false;

    public Member(String id, String username, String password, String email, double accountBalance, double frozenBalance, boolean isBanned) {
        super(id, username, password, email);
        this.accountBalance = accountBalance;
        this.frozenBalance = frozenBalance;
        this.isBanned = isBanned;
    }
    public synchronized double getAccountBalance() {
        return accountBalance;
    }

    public synchronized double getFrozenBalance() {
        return frozenBalance;
    }

    public boolean isBanned() {
        return isBanned;
    }

    public synchronized void setBanned() {
        this.isBanned = true;
    }
    public synchronized void unban() {
        this.isBanned = false;
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
