package vn.io.huangnosimp.controller.handler;

import vn.io.huangnosimp.dto.request.BanMemberRequestDTO;
import vn.io.huangnosimp.dto.request.CancelAuctionRequestDTO;
import vn.io.huangnosimp.dto.response.AuctionCardDTO;
import vn.io.huangnosimp.dto.response.MemberDTO;
import vn.io.huangnosimp.enums.UserType;
import vn.io.huangnosimp.util.GsonParser;
import vn.io.huangnosimp.protocol.Request;
import vn.io.huangnosimp.protocol.Response;
import vn.io.huangnosimp.controller.RequestHandler;
import vn.io.huangnosimp.protocol.ResponseStatus;
import vn.io.huangnosimp.network.ClientHandle;
import vn.io.huangnosimp.service.IAdminService;
import vn.io.huangnosimp.model.Member;
import vn.io.huangnosimp.model.Auction;

import java.util.List;

public class AdminHandler {

    private static boolean isAdmin(ClientHandle client) {
        return UserType.ADMIN.equals(client.getUserType());
    }

    public static class GetAllMembersHandler implements RequestHandler {
        private final IAdminService adminService;

        public GetAllMembersHandler(IAdminService adminService) {
            this.adminService = adminService;
        }

        @Override
        public Response handle(Request request, ClientHandle client) {
            if (!isAdmin(client)) {
                return new Response(ResponseStatus.UNAUTHORIZED, "Access denied: Admins only");
            }
            List<MemberDTO> members = adminService.getAllMembers();
            return new Response(ResponseStatus.SUCCESS, "Get all members successful", members);
        }
    }

    public static class LockMemberHandler implements RequestHandler {
        private final IAdminService adminService;

        public LockMemberHandler(IAdminService adminService) {
            this.adminService = adminService;
        }

        @Override
        public Response handle(Request request, ClientHandle client) {
            if (!isAdmin(client)) return new Response(ResponseStatus.UNAUTHORIZED, "Access denied");

            BanMemberRequestDTO dto = GsonParser.GSON.fromJson(GsonParser.GSON.toJsonTree(request.getData()), BanMemberRequestDTO.class);
            boolean success = adminService.lockMember(dto.getTargetId(), dto.getDurationMinutes());
            if (success) {
                return new Response(ResponseStatus.SUCCESS, "Member locked successfully");
            }
            return new Response(ResponseStatus.FAILED, "Failed to lock member");
        }
    }

    public static class UnlockMemberHandler implements RequestHandler {
        private final IAdminService adminService;

        public UnlockMemberHandler(IAdminService adminService) {
            this.adminService = adminService;
        }

        @Override
        public Response handle(Request request, ClientHandle client) {
            if (!isAdmin(client)) return new Response(ResponseStatus.UNAUTHORIZED, "Access denied");

            BanMemberRequestDTO dto = GsonParser.GSON.fromJson(GsonParser.GSON.toJsonTree(request.getData()), BanMemberRequestDTO.class);
            boolean success = adminService.unlockMember(dto.getTargetId());

            if (success) {
                return new Response(ResponseStatus.SUCCESS, "Member unlocked successfully");
            }
            return new Response(ResponseStatus.FAILED, "Failed to unlock member");
        }
    }

    public static class GetAllAuctionsHandler implements RequestHandler {
        private final IAdminService adminService;

        public GetAllAuctionsHandler(IAdminService adminService) {
            this.adminService = adminService;
        }

        @Override
        public Response handle(Request request, ClientHandle client) {
            if (!isAdmin(client)) return new Response(ResponseStatus.UNAUTHORIZED, "Access denied");

            List<AuctionCardDTO> auctions = adminService.getAllAuctions();
            return new Response(ResponseStatus.SUCCESS, "Get all auctions successful", auctions);
        }
    }

    public static class ForceCancelAuctionHandler implements RequestHandler {
        private final IAdminService adminService;

        public ForceCancelAuctionHandler(IAdminService adminService) {
            this.adminService = adminService;
        }

        @Override
        public Response handle(Request request, ClientHandle client) {
            if (!isAdmin(client)) return new Response(ResponseStatus.UNAUTHORIZED, "Access denied");

            CancelAuctionRequestDTO dto = GsonParser.GSON.fromJson(GsonParser.GSON.toJsonTree(request.getData()), CancelAuctionRequestDTO.class);
            boolean success = adminService.forceCancelAuction(dto.getAuctionId());

            if (success) {
                return new Response(ResponseStatus.SUCCESS, "Auction force canceled successfully");
            }
            return new Response(ResponseStatus.FAILED, "Force cancel failed");
        }
    }

    public static class GetRevenueHandler implements RequestHandler {
        private final IAdminService adminService;

        public GetRevenueHandler(IAdminService adminService) {
            this.adminService = adminService;
        }

        @Override
        public Response handle(Request request, ClientHandle client) {
            if (!isAdmin(client)) return new Response(ResponseStatus.UNAUTHORIZED, "Access denied");

            double revenue = adminService.getSystemTotalRevenue();
            return new Response(ResponseStatus.SUCCESS, "Revenue calculated", revenue);
        }
    }
}