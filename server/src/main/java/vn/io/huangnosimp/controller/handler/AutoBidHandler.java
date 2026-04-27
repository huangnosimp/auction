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
            if (userId == null) return new Response(ResponseStatus.UNAUTHORIZED, "Unauthorized");

            try {
                AutoBidRequestDTO data = GsonParser.GSON.fromJson(
                        GsonParser.GSON.toJsonTree(request.getData()),
                        AutoBidRequestDTO.class
                );

                if (data == null) {
                    return new Response(ResponseStatus.ERROR, "Invalid request data.");
                }

                boolean success = autoBidService.registerAutoBid(
                        userId, data.getAuctionId(), data.getMaxBid(), data.getIncrement()
                );

                return success ? new Response(ResponseStatus.SUCCESS, "Registered")
                        : new Response(ResponseStatus.ERROR, "Failed");
            } catch (Exception e) {
                return new Response(ResponseStatus.ERROR, "Invalid data");
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
            if (userId == null) return new Response(ResponseStatus.UNAUTHORIZED, "Unauthorized");

            try {
                AutoBidRequestDTO data = GsonParser.GSON.fromJson(
                        GsonParser.GSON.toJsonTree(request.getData()),
                        AutoBidRequestDTO.class
                );

                if (data == null) {
                    return new Response(ResponseStatus.ERROR, "Invalid request data.");
                }

                autoBidService.unregisterAutoBid(userId, data.getAuctionId());
                return new Response(ResponseStatus.SUCCESS, "Unregistered");
            } catch (Exception e) {
                return new Response(ResponseStatus.ERROR, "Invalid data");
            }
        }
    }
}