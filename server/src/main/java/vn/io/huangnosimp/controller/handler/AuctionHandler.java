package vn.io.huangnosimp.controller.handler;

import vn.io.huangnosimp.dto.response.*;
import vn.io.huangnosimp.network.ClientHandle;
import vn.io.huangnosimp.service.IUserService;
import vn.io.huangnosimp.util.GsonParser;
import vn.io.huangnosimp.dto.request.*;
import vn.io.huangnosimp.protocol.Request;
import vn.io.huangnosimp.protocol.Response;
import vn.io.huangnosimp.controller.RequestHandler;
import vn.io.huangnosimp.protocol.ResponseStatus;
import vn.io.huangnosimp.service.IAuctionService;

public class AuctionHandler {
    public static class CreateAuctionHandler implements RequestHandler {
        private final IAuctionService auctionService;

        public CreateAuctionHandler(IAuctionService auctionService) {
            this.auctionService = auctionService;
        }

        @Override
        public Response handle(Request request, ClientHandle client) {
            CreateAuctionRequestDTO dto = GsonParser.GSON.fromJson(GsonParser.GSON.toJsonTree(request.getData()), CreateAuctionRequestDTO.class);
            AuctionCardDTO auctionCardDTO = auctionService.createAuction(
                    client.getUserId(), dto.getItemName(), dto.getDescription(),
                    dto.getItemType(), dto.getAttributes(), dto.getStartPrice(),
                    dto.getStartTime(), dto.getEndTime(), dto.getCondition(),
                    dto.getMinimumIncrement(), dto.getBuyNowPrice());
            if (auctionCardDTO == null) {
                return new Response(ResponseStatus.FAILED, "Create auction failed");
            }
            return new Response(ResponseStatus.SUCCESS, "Create auction successfully", auctionCardDTO);
        }
    }

    public static class CancelAuctionHandler implements RequestHandler {
        private final IAuctionService auctionService;

        public CancelAuctionHandler(IAuctionService auctionService) {
            this.auctionService = auctionService;
        }

        @Override
        public Response handle(Request request, ClientHandle client) {
            CancelAuctionRequestDTO dto = GsonParser.GSON.fromJson(GsonParser.GSON.toJsonTree(request.getData()), CancelAuctionRequestDTO.class);
            AuctionActionResult result = auctionService.cancelAuction(dto.getAuctionId());
            return switch (result) {
                case SUCCESS -> new Response(ResponseStatus.SUCCESS, "Cancel auction successfully");
                case AUCTION_NOT_FOUND -> new Response(ResponseStatus.FAILED, "Auction not found");
                case INVALID_STATE -> new Response(ResponseStatus.FAILED, "Cannot cancel a running or completed auction");
                case UNAUTHORIZED -> new Response(ResponseStatus.FAILED, "Unauthorized to cancel this auction");
                default -> new Response(ResponseStatus.FAILED, "Cancel auction failed");
            };
        }
    }

    public static class PlaceBidHandler implements RequestHandler {
        private final IAuctionService auctionService;
        private final IUserService userService;

        public PlaceBidHandler(IAuctionService auctionService, IUserService userService) {
            this.auctionService = auctionService;
            this.userService = userService;
        }

        @Override
        public Response handle(Request request, ClientHandle client) {
            PlaceBidRequestDTO dto = GsonParser.GSON.fromJson(GsonParser.GSON.toJsonTree(request.getData()), PlaceBidRequestDTO.class);
            BidResult result = auctionService.placeBid(client.getUserId(), dto.getAuctionId(), dto.getBidAmount(), false);
            PlaceBidResponseDTO responseDTO = new PlaceBidResponseDTO(userService.getMember(client.getUserId()).getUsername(), dto.getBidAmount(), System.currentTimeMillis());
            return switch (result) {
                case SUCCESS -> new Response(ResponseStatus.SUCCESS, "Place bid successfully", responseDTO);
                case AUCTION_NOT_FOUND -> new Response(ResponseStatus.FAILED, "Auction not found");
                case AUCTION_ENDED -> new Response(ResponseStatus.FAILED, "Auction has already ended");
                case INSUFFICIENT_FUNDS -> new Response(ResponseStatus.FAILED, "Insufficient funds");
                case BID_TOO_LOW -> new Response(ResponseStatus.FAILED, "Bid amount is too low");
                case ALREADY_HIGHEST_BIDDER -> new Response(ResponseStatus.FAILED, "You are already the highest bidder");
                case NOT_IN_ROOM -> new Response(ResponseStatus.FAILED, "You must join the auction room before placing a bid");
                default -> new Response(ResponseStatus.FAILED, "Place bid failed");
            };
        }
    }
    public static class JoinRoomHandler implements RequestHandler {
        private final IAuctionService auctionService;
        public JoinRoomHandler(IAuctionService auctionService) {
            this.auctionService = auctionService;
        }
        @Override
        public Response handle(Request request, ClientHandle client) {
            JoinRoomRequestDTO dto = GsonParser.GSON.fromJson(GsonParser.GSON.toJsonTree(request.getData()), JoinRoomRequestDTO.class);
            AuctionActionResult result = auctionService.joinAuction(client.getUserId(), dto.getAuctionId(), client);
            return switch (result) {
                case SUCCESS -> new Response(ResponseStatus.SUCCESS, "Join room successfully");
                case AUCTION_NOT_FOUND -> new Response(ResponseStatus.FAILED, "Auction not found");
                case INVALID_STATE -> new Response(ResponseStatus.FAILED, "Auction is not open for joining");
                default -> new Response(ResponseStatus.FAILED, "Join room failed");
            };
        }
    }
    public static class LeaveRoomHandler implements RequestHandler {
        private final IAuctionService auctionService;

        public LeaveRoomHandler(IAuctionService auctionService) {
            this.auctionService = auctionService;
        }

        @Override
        public Response handle(Request request, ClientHandle client) {
            LeaveRoomRequestDTO dto = GsonParser.GSON.fromJson(GsonParser.GSON.toJsonTree(request.getData()), LeaveRoomRequestDTO.class);
            AuctionActionResult result = auctionService.leaveAuction(client.getUserId(), dto.getAuctionId(), client);
            return switch (result) {
                case SUCCESS -> new Response(ResponseStatus.SUCCESS, "Leave room successfully");
                case AUCTION_NOT_FOUND -> new Response(ResponseStatus.FAILED, "Auction not found");
                default -> new Response(ResponseStatus.FAILED, "Leave room failed");
            };
        }
    }
    public static class BuyNowHandler implements RequestHandler {
        private final IAuctionService auctionService;

        public BuyNowHandler(IAuctionService auctionService) {
            this.auctionService = auctionService;
        }

        @Override
        public Response handle(Request request, ClientHandle client) {
            BuyNowRequestDTO dto = GsonParser.GSON.fromJson(GsonParser.GSON.toJsonTree(request.getData()), BuyNowRequestDTO.class);
            BidResult result = auctionService.buyNow(client.getUserId(), dto.getAuctionId());
            return switch (result) {
                case SUCCESS -> new Response(ResponseStatus.SUCCESS, "Buy now successfully");
                case AUCTION_NOT_FOUND -> new Response(ResponseStatus.FAILED, "Auction not found");
                case AUCTION_ENDED -> new Response(ResponseStatus.FAILED, "Auction has already ended");
                case INSUFFICIENT_FUNDS -> new Response(ResponseStatus.FAILED, "Insufficient funds");
                default -> new Response(ResponseStatus.FAILED, "Buy now failed");
            };
        }
    }
}