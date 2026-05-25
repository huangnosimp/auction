package vn.io.huangnosimp.dto.shared;

public class AuctionExtendedDTO {
    private final long newEndTime;
    private final long serverTimeMillis;

    public AuctionExtendedDTO(long newEndTime, long serverTimeMillis) {
        this.newEndTime = newEndTime;
        this.serverTimeMillis = serverTimeMillis;
    }

    public long getNewEndTime() {
        return newEndTime;
    }

    public long getServerTimeMillis() {
        return serverTimeMillis;
    }
}
