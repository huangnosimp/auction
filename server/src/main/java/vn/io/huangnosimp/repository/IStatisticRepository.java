package vn.io.huangnosimp.repository;

public interface IStatisticRepository {
    double getAccountBalance(String userId);
    int getJoinedActiveRoomsCount(String userId);
    int getWinningBidsCount(String userId);
    int getOutBidsCount(String userId);
    int getWonTotalCount(String userId);
}
