package vn.io.huangnosimp.controller.handler;

import vn.io.huangnosimp.controller.RequestHandler;
import vn.io.huangnosimp.dto.request.GetAuctionDetailRequestDTO;
import vn.io.huangnosimp.dto.response.AuctionCardDTO;
import vn.io.huangnosimp.dto.response.DashboardResponseDTO;
import vn.io.huangnosimp.network.ClientHandle;
import vn.io.huangnosimp.protocol.Request;
import vn.io.huangnosimp.protocol.Response;
import vn.io.huangnosimp.protocol.ResponseStatus;
import vn.io.huangnosimp.service.IStatisticService;
import vn.io.huangnosimp.util.GsonParser;

public class StatisticHandler {
    public static class GetDashboardInfoHandler implements RequestHandler {
        private final IStatisticService statisticService;

        public GetDashboardInfoHandler(IStatisticService statisticService) {
            this.statisticService = statisticService;
        }

        @Override
        public Response handle(Request request, ClientHandle client) {
            DashboardResponseDTO dashboardInfo = statisticService.getDashboardStatistics(client.getUserId());
            if (dashboardInfo == null) {
                return new Response(ResponseStatus.FAILED, "Failed to retrieve dashboard information");
            }
            return new Response(ResponseStatus.SUCCESS, dashboardInfo);
        }
    }
    public static class GetAuctionDetailHandler implements RequestHandler {
        private final IStatisticService statisticService;

        public GetAuctionDetailHandler(IStatisticService statisticService) {
            this.statisticService = statisticService;
        }

        @Override
        public Response handle(Request request, ClientHandle client) {
            GetAuctionDetailRequestDTO dto = GsonParser.GSON.fromJson(GsonParser.GSON.toJsonTree(request.getData()), GetAuctionDetailRequestDTO.class);
            if (dto.getAuctionId() == null || dto.getAuctionId().isBlank()) {
                return new Response(ResponseStatus.FAILED, "Invalid auction ID");
            }
            return new Response(ResponseStatus.SUCCESS, statisticService.getAuctionDetail(client.getUserId(), dto.getAuctionId()));
        }
    }
    public static class GetAuctionCardHandler implements RequestHandler {
        private final IStatisticService statisticService;
        public GetAuctionCardHandler(IStatisticService statisticService) {
            this.statisticService = statisticService;
        }

        @Override
        public Response handle(Request request, ClientHandle client) {
            AuctionCardDTO auctionCardDTO = statisticService.getAuctionCard(client.getUserId());
            if (auctionCardDTO == null) {
                return new Response(ResponseStatus.FAILED, "No active auction found");
            }
            return new Response(ResponseStatus.SUCCESS, auctionCardDTO);
        }
    }
}
