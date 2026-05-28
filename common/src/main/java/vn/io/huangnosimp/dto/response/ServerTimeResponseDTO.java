package vn.io.huangnosimp.dto.response;

public class ServerTimeResponseDTO {
    private final long serverTimeMillis;

    public ServerTimeResponseDTO(long serverTimeMillis) {
        this.serverTimeMillis = serverTimeMillis;
    }

    public long getServerTimeMillis() {
        return serverTimeMillis;
    }
}
