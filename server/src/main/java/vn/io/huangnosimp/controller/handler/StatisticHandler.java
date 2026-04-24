package vn.io.huangnosimp.controller.handler;

import vn.io.huangnosimp.controller.RequestHandler;
import vn.io.huangnosimp.network.ClientHandle;
import vn.io.huangnosimp.protocol.Request;
import vn.io.huangnosimp.protocol.Response;
import vn.io.huangnosimp.protocol.ResponseStatus;
import vn.io.huangnosimp.service.IStatisticService;

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
}
