package vn.io.huangnosimp.repository;

public interface IAuctionParticipantsRepository {
    void addParticipant(String auctionId, String userId);
    void removeParticipant(String auctionId, String userId);
    boolean isParticipant(String auctionId, String userId);
}
