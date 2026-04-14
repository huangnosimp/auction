package vn.io.huangnosimp.controller;

import vn.io.huangnosimp.model.ActionType;
import vn.io.huangnosimp.model.ResponseStatus;
import vn.io.huangnosimp.network.ClientHandle;
import vn.io.huangnosimp.network.Request;
import vn.io.huangnosimp.network.Response;

public class MessageRouter {
    private UserController userController;
    private AuctionController auctionController;

    public MessageRouter() {
        this.userController = new UserController();
        this.auctionController = new AuctionController();
    }

    public Response route(Request request, ClientHandle client) {
        ActionType action = request.getAction();

        if (action == null) {
            return new Response(ResponseStatus.ERROR, "Yêu cầu không hợp lệ: thiếu trường 'action'");
        }

        System.out.println("[Router] Đang điều hướng gói tin: " + action);

        return switch (action) {
            case LOGIN -> userController.handleLogin(request, client);
            case REGISTER -> userController.handleRegister(request);
            case LEAVE_AUCTION -> userController.handleLeaveAuction(request, client);
            case JOIN_AUCTION -> userController.handleJoinAuction(request, client);

            case GET_ACTIVE_AUCTIONS -> auctionController.handleGetActiveAuctions();
            case GET_JOINED_AUCTION -> auctionController.handleGetJoinedAuctions(client);
            case GET_POSTED_AUCTION -> auctionController.handleGetPostedAuctions(client);
            case GET_AUCTION_DETAIL -> auctionController.handleGetAuctionDetail(request);
            case CREATE_AUCTION -> auctionController.handleCreateAuction(request, client);
            case CANCEL_AUCTION -> auctionController.handleCancelAuction(request);
            case PLACE_BID -> auctionController.handlePlaceBid(request, client);
            
            default -> new Response(ResponseStatus.ERROR, "Hành động không được hỗ trợ: " + action);
        };
    }
}
