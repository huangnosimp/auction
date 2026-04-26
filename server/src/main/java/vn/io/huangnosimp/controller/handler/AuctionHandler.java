package vn.io.huangnosimp.controller.handler;

import vn.io.huangnosimp.dto.response.AuctionDetailResponseDTO;
import vn.io.huangnosimp.network.ClientHandle;
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
            AuctionDetailResponseDTO auctionResponseDTO = auctionService.createAuction(client.getUserId(), dto.getItemName(), dto.getDescription(), dto.getItemType(), dto.getAttributes(), dto.getStartPrice(), dto.getStartTime(), dto.getEndTime());
            if (auctionResponseDTO == null) {
                return new Response(ResponseStatus.FAILED, "Create auction failed");
            }
            return new Response(ResponseStatus.SUCCESS, "Create auction successfully", auctionResponseDTO);
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
            boolean result = auctionService.cancelAuction(dto.getAuctionId());
            if (result) {
                return new Response(ResponseStatus.SUCCESS, "Cancel auction successfully");
            } else {
                return new Response(ResponseStatus.FAILED, "Cancel auction failed");
            }
        }
    }

    public static class PlaceBidHandler implements RequestHandler {
        private final IAuctionService auctionService;

        public PlaceBidHandler(IAuctionService auctionService) {
            this.auctionService = auctionService;
        }

        @Override
        public Response handle(Request request, ClientHandle client) {
            PlaceBidRequestDTO dto = GsonParser.GSON.fromJson(GsonParser.GSON.toJsonTree(request.getData()), PlaceBidRequestDTO.class);
            boolean result = auctionService.placeBid(client.getUserId(), dto.getAuctionId(), dto.getBidAmount(), false);
            if (result) {
                return new Response(ResponseStatus.SUCCESS, "Place bid successfully");
            } else {
                return new Response(ResponseStatus.FAILED, "Place bid failed");
            }
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
            boolean result = auctionService.joinAuction(client.getUserId(), dto.getAuctionId(), client);
            if (result) {
                return new Response(ResponseStatus.SUCCESS, "Join room successfully");
            }
            return new Response(ResponseStatus.FAILED, "Join room failed");
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
            boolean result = auctionService.leaveAuction(client.getUserId(), dto.getAuctionId(), client);
            if (result) {
                return new Response(ResponseStatus.SUCCESS, "Leave room successfully");
            }
            return new Response(ResponseStatus.FAILED, "Leave room failed");
        }
    }
}