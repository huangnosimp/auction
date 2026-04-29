package vn.io.huangnosimp.controller.handler;

import vn.io.huangnosimp.controller.RequestHandler;
import vn.io.huangnosimp.dto.request.GetAuctionDetailRequestDTO;
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
            return new Response(ResponseStatus.SUCCESS, statisticService.getDashboardStatistics(client.getUserId()));
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
}
