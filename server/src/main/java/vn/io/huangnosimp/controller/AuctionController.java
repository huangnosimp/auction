package vn.io.huangnosimp.controller;

import vn.io.huangnosimp.model.ResponseStatus;
import vn.io.huangnosimp.network.*;
import vn.io.huangnosimp.service.AuctionService;

public class AuctionController {
    public Response handleGetActiveAuctions() {
        Response response = new Response(ResponseStatus.SUCCESS, AuctionService.getInstance().getActiveAuction());
        return response;
    }
    public Response handleGetJoinedAuctions(ClientHandle client) {
        Response response = new Response(ResponseStatus.SUCCESS, AuctionService.getInstance().getAuctionByBidder(client.getUserId()));
        return response;
    }
    public Response handleGetPostedAuctions(ClientHandle client) {
        Response response = new Response(ResponseStatus.SUCCESS, AuctionService.getInstance().getAuctionBySeller(client.getUserId()));
        return response;
    }
    public Response handleGetAuctionDetail(Request request) {
        String auctionData = Request.GSON.toJson(request.getData());
        AuctionRequestDTO auctionRequestDTO = Request.GSON.fromJson(auctionData, AuctionRequestDTO.class);
        String auctionId = auctionRequestDTO.getAuctionId();
        Response response = new Response(ResponseStatus.SUCCESS, AuctionService.getInstance().getAuctionDetail(auctionId));
        return response;

    }
    public Message handleCreateAuction (Request request, ClientHandle client) {
        System.out.println("gọi đến AuctionService");
        return null;
    }
    public Response handleCancelAuction(Request request) {
        String auctionData = Request.GSON.toJson(request.getData());
        AuctionRequestDTO auctionRequestDTO = Request.GSON.fromJson(auctionData, AuctionRequestDTO.class);
        String auctionId = auctionRequestDTO.getAuctionId();
        AuctionService.getInstance().cancelAuction(auctionId);
        Response response = new Response(ResponseStatus.SUCCESS, "huỷ thành công!");
        return response;
    }
    public Response handlePlaceBid (Request request, ClientHandle client) {
        String auctionData = Request.GSON.toJson(request.getData());
        AuctionRequestDTO auctionRequestDTO = Request.GSON.fromJson(auctionData, AuctionRequestDTO.class);
        String auctionId = auctionRequestDTO.getAuctionId();
        double bidAmount = auctionRequestDTO.getAmount();
        if (AuctionService.getInstance().placeBid(auctionId, client.getUserId(), bidAmount)) {
            Response response = new Response(ResponseStatus.SUCCESS, "Đặt giá thành công!");
            return response;
        } else {
            Response response = new Response(ResponseStatus.ERROR, "Đặt giá thất bại! Vui lòng thử lại.");
            return response;
        }
    }
}
