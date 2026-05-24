package vn.io.huangnosimp.repository;

import vn.io.huangnosimp.model.Transaction;

public interface ITransactionRepository {
    boolean saveTransaction(Transaction transaction);
}
