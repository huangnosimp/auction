package vn.io.huangnosimp.controller.handler;

import com.google.gson.Gson;
import vn.io.huangnosimp.controller.RequestHandler;
import vn.io.huangnosimp.dto.request.AutoBidRequestDTO;
import vn.io.huangnosimp.network.ClientHandle;
import vn.io.huangnosimp.protocol.Request;
import vn.io.huangnosimp.protocol.Response;
import vn.io.huangnosimp.protocol.ResponseStatus;
import vn.io.huangnosimp.service.IAutoBidService;
import vn.io.huangnosimp.util.GsonParser;

public class AutoBidHandler {

    public static class RegisterHandler implements RequestHandler {
        private final IAutoBidService autoBidService;

        public RegisterHandler(IAutoBidService autoBidService) {
            this.autoBidService = autoBidService;
        }

        @Override
        public Response handle(Request request, ClientHandle client) {
            String userId = client.getUserId();
            if (userId == null) return new Response(ResponseStatus.UNAUTHORIZED, "Unauthorized. Please log in.");

            try {
                AutoBidRequestDTO data = GsonParser.GSON.fromJson(
                        GsonParser.GSON.toJsonTree(request.getData()),
                        AutoBidRequestDTO.class
                );

                if (data == null || data.getAuctionId() == null || data.getAuctionId().trim().isEmpty()) {
                    return new Response(ResponseStatus.ERROR, "Invalid auction data.");
                }
                if (data.getMaxBid() <= 0) {
                    return new Response(ResponseStatus.ERROR, "Maximum bid must be greater than 0.");
                }

                boolean success = autoBidService.registerAutoBid(
                        userId, data.getAuctionId(), data.getMaxBid(), data.getIncrement()
                );

                return success
                        ? new Response(ResponseStatus.SUCCESS, "Auto-Bid registered successfully!")
                        : new Response(ResponseStatus.ERROR, "Could not register Auto-Bid at this time.");

            } catch (IllegalArgumentException | IllegalStateException e) {
                return new Response(ResponseStatus.ERROR, e.getMessage());
            } catch (Exception e) {
                System.err.println("[AutoBid Register Error]: " + e.getMessage());
                e.printStackTrace();
                return new Response(ResponseStatus.ERROR, "Internal server error: " + e.getMessage());
            }
        }
    }

    public static class UnregisterHandler implements RequestHandler {
        private final IAutoBidService autoBidService;

        public UnregisterHandler(IAutoBidService autoBidService) {
            this.autoBidService = autoBidService;
        }

        @Override
        public Response handle(Request request, ClientHandle client) {
            String userId = client.getUserId();
            if (userId == null) return new Response(ResponseStatus.UNAUTHORIZED, "Unauthorized. Please log in.");

            try {
                AutoBidRequestDTO data = GsonParser.GSON.fromJson(
                        GsonParser.GSON.toJsonTree(request.getData()),
                        AutoBidRequestDTO.class
                );

                if (data == null || data.getAuctionId() == null) {
                    return new Response(ResponseStatus.ERROR, "Invalid request data.");
                }

                autoBidService.unregisterAutoBid(userId, data.getAuctionId());
                return new Response(ResponseStatus.SUCCESS, "Auto-Bid unregistered successfully.");

            } catch (IllegalArgumentException | IllegalStateException e) {
                return new Response(ResponseStatus.ERROR, e.getMessage());
            } catch (Exception e) {
                System.err.println("[AutoBid Unregister Error]: " + e.getMessage());
                e.printStackTrace();
                return new Response(ResponseStatus.ERROR, "Internal server error during unregistration.");
            }
        }
    }
}