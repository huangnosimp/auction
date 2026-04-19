package vn.io.huangnosimp.repository;

import vn.io.huangnosimp.model.BidTransaction;

public interface ITransactionRepository {
    void saveTransaction(BidTransaction transaction, String sellerId);
}
