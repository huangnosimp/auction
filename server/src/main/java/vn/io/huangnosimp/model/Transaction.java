package vn.io.huangnosimp.model;

import vn.io.huangnosimp.enums.TransactionType;

public class Transaction extends Entity {

    private TransactionType transactionType;

    private String userId;

    private double amount;

    public Transaction(String userId, TransactionType transactionType, double amount) {
        super();
        super.setCreatedAt(System.currentTimeMillis());
        this.userId = userId;
        this.transactionType = transactionType;
        this.amount = amount;
    }

    public Transaction(String id, String userId, TransactionType transactionType, double amount, long createdAt) {
        super(id, createdAt);
        super.setCreatedAt(createdAt);
        this.userId = userId;
        this.transactionType = transactionType;
        this.amount = amount;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
