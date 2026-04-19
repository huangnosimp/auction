package vn.io.huangnosimp.dto.shared;

import vn.io.huangnosimp.enums.NotificationType;

public class NotificationDTO {
    private final NotificationType notificationType;
    private final String auctionId;
    private final String message;
    private final long timestamp;

    public NotificationDTO(NotificationType notificationType, String auctionId, String message) {
        this.notificationType = notificationType;
        this.auctionId = auctionId;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
