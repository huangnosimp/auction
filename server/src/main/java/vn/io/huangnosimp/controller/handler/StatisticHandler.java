package vn.io.huangnosimp.controller.handler;

import vn.io.huangnosimp.controller.RequestHandler;
import vn.io.huangnosimp.dto.request.GetAuctionDetailRequestDTO;
import vn.io.huangnosimp.dto.request.GetJoiningAuctionCardDTO;
import vn.io.huangnosimp.dto.request.GetPublicAcutionCardDTO;
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
    public static class GetJoiningAuctionCardHandler implements RequestHandler {
        private final IStatisticService statisticService;
        public GetJoiningAuctionCardHandler(IStatisticService statisticService) {
            this.statisticService = statisticService;
        }

        @Override
        public Response handle(Request request, ClientHandle client) {
            GetJoiningAuctionCardDTO dto = GsonParser.GSON.fromJson(GsonParser.GSON.toJsonTree(request.getData()), GetJoiningAuctionCardDTO.class);
             if (dto.getAuctionId() == null || dto.getAuctionId().isBlank()) {
                return new Response(ResponseStatus.FAILED, "Invalid auction ID");
            }
            return new Response(ResponseStatus.SUCCESS, statisticService.getJoiningAuctionCard(dto.getAuctionId()));
        }
    }
    public static class GetPublicAuctionCardHandler implements RequestHandler {
        private final IStatisticService statisticService;
        public GetPublicAuctionCardHandler(IStatisticService statisticService) {
            this.statisticService = statisticService;
        }

        @Override
        public Response handle(Request request, ClientHandle client) {
            GetPublicAcutionCardDTO dto = GsonParser.GSON.fromJson(GsonParser.GSON.toJsonTree(request.getData()), GetPublicAcutionCardDTO.class);
             if (dto.getQuantity() <= 0) {
                 return new Response(ResponseStatus.FAILED, "Invalid quantity");
             }
            return new Response(ResponseStatus.SUCCESS, statisticService.getPublicAuctionCard(dto.getQuantity()));
        }
    }
}
