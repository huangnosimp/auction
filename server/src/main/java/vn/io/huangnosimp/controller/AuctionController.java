package vn.io.huangnosimp.controller;

import java.util.Collection;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import vn.io.huangnosimp.model.Auction;
import vn.io.huangnosimp.model.Message;
import vn.io.huangnosimp.service.AuctionManager;
import vn.io.huangnosimp.service.AuctionService;
import vn.io.huangnosimp.service.BiddingService;

public class AuctionController {
    private final BiddingService biddingService;
    private final AuctionService auctionService;
    private final AuctionManager auctionManager;
    private final Gson gson;

    public AuctionController(BiddingService biddingService, AuctionService auctionService) {
        this.biddingService = biddingService;
        this.auctionService = auctionService;
        this.auctionManager = AuctionManager.getInstance();
        this.gson = new Gson();
    }

    public Message handleGetActiveAuctions(Message request) {
        try {
            Collection<Auction> activeAuctions = auctionManager.getAllActiveAuctions();

            JsonArray auctionsArray = new JsonArray();
            for (Auction auction : activeAuctions) {
                JsonObject aucJson = new JsonObject();
                aucJson.addProperty("auctionId", auction.getId());
                aucJson.addProperty("itemName", auction.getItem() != null ? auction.getItem().getName() : "Unknown Item");
                aucJson.addProperty("currentPrice", auction.getCurrentPrice());
                aucJson.addProperty("endTime", auction.getEndTime().toString());

                auctionsArray.add(aucJson);
            }
            JsonObject responseData = new JsonObject();
            responseData.add("auctions", auctionsArray);
            return new Message("AUCTION_LIST", responseData.toString());

        } catch (Exception e) {
            return new Message("ERROR", "{\"message\": \"Lỗi khi lấy danh sách phiên đấu giá\"}");
        }
    }

    public Message handleCreateAuction(Message request) {
        try {
            JsonObject data = JsonParser.parseString(request.getData()).getAsJsonObject();
            JsonObject responseData = new JsonObject();
            responseData.addProperty("success", true);
            responseData.addProperty("message", "Tạo phiên đấu giá thành công!");

            return new Message("CREATE_SUCCESS", responseData.toString());
        } catch (Exception e) {
            return new Message("ERROR", "{\"success\": false, \"message\": \"Lỗi tạo phiên đấu giá\"}");
        }
    }

    public Message handlePlaceBid(Message request) {
        try {
            JsonObject data = JsonParser.parseString(request.getData()).getAsJsonObject();
            String auctionId = data.get("auctionId").getAsString();
            String bidderId = data.get("bidderId").getAsString();
            double amount = data.get("amount").getAsDouble();

            biddingService.placeBid(auctionId, bidderId, amount);

            JsonObject responseData = new JsonObject();
            responseData.addProperty("success", true);
            responseData.addProperty("message", "Đặt giá thành công!");

            return new Message("BID_SUCCESS", responseData.toString());

        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println("[AuctionController] Lỗi logic đặt giá: " + e.getMessage());
            JsonObject errorData = new JsonObject();
            errorData.addProperty("success", false);
            errorData.addProperty("message", e.getMessage());

            return new Message("BID_FAILED", errorData.toString());
        } catch (Exception e) {
            System.err.println("[AuctionController] Lỗi parse JSON hoặc hệ thống: " + e.getMessage());
            return new Message("ERROR", "{\"success\": false, \"message\": \"Dữ liệu không hợp lệ!\"}");
        }
    }
}
