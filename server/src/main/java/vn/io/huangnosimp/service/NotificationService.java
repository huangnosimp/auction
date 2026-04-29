package vn.io.huangnosimp.service;

import vn.io.huangnosimp.protocol.ActionType;
import vn.io.huangnosimp.enums.NotificationType;
import vn.io.huangnosimp.network.ClientSessionManager;
import vn.io.huangnosimp.dto.shared.NotificationDTO;
import vn.io.huangnosimp.protocol.Request;

public class NotificationService {
    private void sendNotification(String auctionId, NotificationType type, String messageText) {
         NotificationDTO dto = new NotificationDTO(type, auctionId, messageText);
         Request request = new Request(ActionType.NOTIFICATION, dto);
         ClientSessionManager.getInstance().broadcastToRoom(auctionId, request);
    }
    public void notifyBidPlaced(String auctionId, double currentPrice, String winnerUsername) {
        String msg = String.format("A new bid of %.2f was placed! Current top bidder: %s", currentPrice, winnerUsername);
        sendNotification(auctionId, NotificationType.NEW_BID, msg);
    }
    public void notifyAuctionEnded(String auctionId, String winnerUsername, double finalPrice) {
        String msg = String.format("Auction ended! Winner: %s with price %.2f", winnerUsername != null ? winnerUsername : "None", finalPrice);
        sendNotification(auctionId, NotificationType.AUCTION_ENDED, msg);
    }
    public void notifyAuctionCanceled(String auctionId) {
        sendNotification(auctionId, NotificationType.AUCTION_CANCELED, "The auction has been canceled.");
    }
    public void notifyOutbid(String auctionId, String userId, double currentPrice) {
        if (!ClientSessionManager.getInstance().isUserInRoom(auctionId, userId)) {
            String msg = String.format("You have been outbid! Current price is %.2f in room %s", currentPrice, auctionId);
            NotificationDTO dto = new NotificationDTO(NotificationType.OUTBID, auctionId, msg);
            Request request = new Request(ActionType.NOTIFICATION, dto);
            ClientSessionManager.getInstance().sendToUser(userId, request);
        }
    }
}
