package vn.io.huangnosimp.dto.shared;

import vn.io.huangnosimp.enums.NotificationType;

public class NotificationDTO {
    private final NotificationType notificationType;
    private final String auctionId;
    private final Object data;
    private final long timestamp;

    public NotificationDTO(NotificationType notificationType, String auctionId, Object data) {
        this.notificationType = notificationType;
        this.auctionId = auctionId;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public Object getData() {
        return data;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
