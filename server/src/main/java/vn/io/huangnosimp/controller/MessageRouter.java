package vn.io.huangnosimp.controller;

import vn.io.huangnosimp.model.Message;

public class MessageRouter {
    private UserController userController;
    private AuctionController auctionController;

    public MessageRouter() {
        this.userController = new UserController();
        this.auctionController = new AuctionController();
    }

    public Message route(Message request) {
        String action = request.getAction();

        if (action == null) {
            return new Message("ERROR", "{\"message\": \"Hành động (action) không được để trống!\"}");
        }

        System.out.println("[Router] Đang điều hướng gói tin: " + action);

        return switch (action) {
            case "LOGIN" -> userController.handleLogin(request);
            case "REGISTER" -> userController.handleRegister(request);

            case "GET_ACTIVE_AUCTIONS" -> auctionController.handleGetActiveAuctions(request);
            case "CREATE_AUCTION" -> auctionController.handleCreateAuction(request);
            case "PLACE_BID" -> auctionController.handlePlaceBid(request);
            
            default -> new Message("ERROR", "{\"message\": \"Hệ thống không hỗ trợ lệnh: " + action + "\"}");
        };
    }
}
