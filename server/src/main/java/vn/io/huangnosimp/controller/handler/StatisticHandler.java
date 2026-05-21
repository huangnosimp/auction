package vn.io.huangnosimp.controller.handler;

import vn.io.huangnosimp.controller.RequestHandler;
import vn.io.huangnosimp.dto.request.GetAuctionDetailRequestDTO;
import vn.io.huangnosimp.dto.request.GetEndedPostedAuctionDTO;
import vn.io.huangnosimp.dto.request.GetJoiningAuctionCardDTO;
import vn.io.huangnosimp.dto.request.GetPostedAuctionDTO;
import vn.io.huangnosimp.dto.request.GetPublicAcutionCardDTO;
import vn.io.huangnosimp.dto.request.GetWonAuctionDTO;
import vn.io.huangnosimp.dto.response.DashboardResponseDTO;
import vn.io.huangnosimp.dto.response.GetPostedAuctionCardResponseDTO;
import vn.io.huangnosimp.dto.response.GetPublicAuctionCardResponseDTO;
import vn.io.huangnosimp.network.ClientHandle;
import vn.io.huangnosimp.protocol.Request;
import vn.io.huangnosimp.protocol.Response;
import vn.io.huangnosimp.protocol.ResponseStatus;
import vn.io.huangnosimp.service.IStatisticService;
import vn.io.huangnosimp.util.GsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatisticHandler {
    private static final Logger logger = LoggerFactory.getLogger(StatisticHandler.class);

    public static class GetDashboardInfoHandler implements RequestHandler {
        private final IStatisticService statisticService;

        public GetDashboardInfoHandler(IStatisticService statisticService) {
            this.statisticService = statisticService;
        }

        @Override
        public Response handle(Request request, ClientHandle client) {
            DashboardResponseDTO dashboardInfo = statisticService.getDashboardStatistics(client.getUserId());
            logger.info("Fetched dashboard info userId={}", client.getUserId());
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
                logger.warn("Invalid auction detail request userId={}", client.getUserId());
                return new Response(ResponseStatus.FAILED, "Invalid auction ID");
            }
            logger.info("Fetched auction detail userId={} auctionId={}", client.getUserId(), dto.getAuctionId());
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
                logger.warn("Invalid joining auction card request userId={}", client.getUserId());
                return new Response(ResponseStatus.FAILED, "Invalid auction ID");
            }
            logger.info("Fetched joining auction card userId={} auctionId={}", client.getUserId(), dto.getAuctionId());
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
                 logger.warn("Invalid public auction card request userId={} quantity={}", client.getUserId(), dto.getQuantity());
                 return new Response(ResponseStatus.FAILED, "Invalid quantity");
            }
            GetPublicAuctionCardResponseDTO dtoResponse = new GetPublicAuctionCardResponseDTO(statisticService.getPublicAuctionCard(dto.getQuantity()));
            logger.info("Fetched public auction cards userId={} quantity={}", client.getUserId(), dto.getQuantity());
            return new Response(ResponseStatus.SUCCESS, dtoResponse);
        }
    }
    public static class GetPostedAuctionCardHandler implements RequestHandler {
        private final IStatisticService statisticService;
        public GetPostedAuctionCardHandler(IStatisticService statisticService) {
            this.statisticService = statisticService;
        }
        @Override
        public Response handle(Request request, ClientHandle client) {
            GetPostedAuctionDTO dto = GsonParser.GSON.fromJson(GsonParser.GSON.toJsonTree(request.getData()), GetPostedAuctionDTO.class);
            if (dto == null || dto.getAmount() <= 0) {
                logger.warn("Invalid posted auction card request userId={} amount={}", client.getUserId(), dto == null ? null : dto.getAmount());
                return new Response(ResponseStatus.FAILED, "Invalid amount");
            }

            GetPostedAuctionCardResponseDTO dtoResponse = new GetPostedAuctionCardResponseDTO(statisticService.getPostedAuctionCard(client.getUserId(), dto.getAmount()));
            logger.info("Fetched posted auction cards userId={} amount={}", client.getUserId(), dto.getAmount());
            return new Response(ResponseStatus.SUCCESS, dtoResponse);
        }
    }
    public static class GetWonAuctionHandler implements RequestHandler {
        private final IStatisticService statisticService;
        public GetWonAuctionHandler(IStatisticService statisticService) {
            this.statisticService = statisticService;
        }
        @Override
        public Response handle(Request request, ClientHandle client) {
            GetWonAuctionDTO dto = GsonParser.GSON.fromJson(GsonParser.GSON.toJsonTree(request.getData()), GetWonAuctionDTO.class);
            if (dto == null || dto.getAmount() <= 0) {
                logger.warn("Invalid won auction request userId={} amount={}", client.getUserId(), dto == null ? null : dto.getAmount());
                return new Response(ResponseStatus.FAILED, "Invalid amount");
            }

            logger.info("Fetched won auctions userId={} amount={}", client.getUserId(), dto.getAmount());
            return new Response(ResponseStatus.SUCCESS, statisticService.getWonAuction(client.getUserId(), dto.getAmount()));
        }
    }
    public static class GetEndedPostedAuctionHandler implements RequestHandler {
        private final IStatisticService statisticService;
        public GetEndedPostedAuctionHandler(IStatisticService statisticService) {
            this.statisticService = statisticService;
        }
        @Override
        public Response handle(Request request, ClientHandle client) {
            GetEndedPostedAuctionDTO dto = GsonParser.GSON.fromJson(GsonParser.GSON.toJsonTree(request.getData()), GetEndedPostedAuctionDTO.class);
            if (dto == null || dto.getAmount() <= 0) {
                logger.warn("Invalid ended posted auction request userId={} amount={}", client.getUserId(), dto == null ? null : dto.getAmount());
                return new Response(ResponseStatus.FAILED, "Invalid amount");
            }

            logger.info("Fetched ended posted auctions userId={} amount={}", client.getUserId(), dto.getAmount());
            return new Response(ResponseStatus.SUCCESS, statisticService.getEndedPostedAuction(client.getUserId(), dto.getAmount()));
        }
    }
}
