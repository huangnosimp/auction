package vn.io.huangnosimp.controller.handler;

import vn.io.huangnosimp.controller.RequestHandler;
import vn.io.huangnosimp.dto.request.AutoBidRequestDTO;
import vn.io.huangnosimp.dto.response.AutoBidResponseDTO;
import vn.io.huangnosimp.network.ClientHandle;
import vn.io.huangnosimp.protocol.Request;
import vn.io.huangnosimp.protocol.Response;
import vn.io.huangnosimp.protocol.ResponseStatus;
import vn.io.huangnosimp.service.IAutoBidService;
import vn.io.huangnosimp.util.GsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AutoBidHandler {
    private static final Logger logger = LoggerFactory.getLogger(AutoBidHandler.class);

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
                logger.info("Register autobid attempted userId={} auctionId={} success={}", userId, data.getAuctionId(), success);

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
                logger.info("Unregister autobid requested userId={} auctionId={}", userId, data.getAuctionId());
                return new Response(ResponseStatus.SUCCESS, "Unregistered");
            } catch (Exception e) {
                return new Response(ResponseStatus.ERROR, "Invalid data");
            }
        }
    }

    public static class GetUserAutoBidsHandler implements RequestHandler {
        private final IAutoBidService autoBidService;

        public GetUserAutoBidsHandler(IAutoBidService autoBidService) {
            this.autoBidService = autoBidService;
        }

        @Override
        public Response handle(Request request, ClientHandle client) {
            String userId = client.getUserId();
            if (userId == null) return new Response(ResponseStatus.UNAUTHORIZED, "Unauthorized");

            try {
                List<AutoBidResponseDTO> autoBids = autoBidService.getAutoBidsByUserId(userId);
                logger.info("Fetched user autobids userId={} count={}", userId, autoBids.size());
                return new Response(ResponseStatus.SUCCESS, autoBids);

            } catch (Exception e) {
                logger.error("Failed to fetch autobids for userId={}", userId, e);
                return new Response(ResponseStatus.ERROR, "Failed to fetch data");
            }
        }
    }
}
