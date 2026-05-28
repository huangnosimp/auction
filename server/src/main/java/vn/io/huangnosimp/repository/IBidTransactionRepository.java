package vn.io.huangnosimp.repository;

import vn.io.huangnosimp.model.BidTransaction;

public interface IBidTransactionRepository {
    boolean saveBidTransaction(BidTransaction bidTransaction);
}
