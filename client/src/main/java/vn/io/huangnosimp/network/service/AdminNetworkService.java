package vn.io.huangnosimp.network.service;

import vn.io.huangnosimp.dto.request.BanMemberRequestDTO;
import vn.io.huangnosimp.dto.response.AuctionAdminDTO;
import vn.io.huangnosimp.network.SocketClient;
import com.google.gson.reflect.TypeToken;
import vn.io.huangnosimp.dto.request.CancelAuctionRequestDTO;
import vn.io.huangnosimp.dto.response.MemberDTO;
import vn.io.huangnosimp.protocol.ActionType;
import vn.io.huangnosimp.protocol.Request;
import vn.io.huangnosimp.protocol.ResponseStatus;
import vn.io.huangnosimp.util.GsonParser;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AdminNetworkService {
    private final SocketClient socketClient;

    public AdminNetworkService(SocketClient socketClient) {
        this.socketClient = socketClient;
    }

    // 1. Lấy danh sách thành viên
    public CompletableFuture<List<MemberDTO>> getAllMembersAsync() {
        Request request = new Request(ActionType.ADMIN_GET_ALL_MEMBERS, null);

        return socketClient.sendRequestAsync(request).thenApply(response -> {
            if (response.getStatus() == ResponseStatus.SUCCESS) {
                Type listType = new TypeToken<List<MemberDTO>>(){}.getType();
                return GsonParser.GSON.fromJson(GsonParser.GSON.toJsonTree(response.getData()), listType);
            }
            return Collections.emptyList();
        });
    }

    // 2. Khóa tài khoản
    public CompletableFuture<Boolean> lockMemberAsync(String memberId, int durationMinutes) {
        BanMemberRequestDTO dto = new BanMemberRequestDTO(memberId, durationMinutes);
        Request request = new Request(ActionType.ADMIN_LOCK_MEMBER, dto);

        return socketClient.sendRequestAsync(request).thenApply(response ->
                response.getStatus() == ResponseStatus.SUCCESS
        );
    }

    // 3. Mở khóa tài khoản
    public CompletableFuture<Boolean> unlockMemberAsync(String memberId) {
        BanMemberRequestDTO dto = new BanMemberRequestDTO(memberId, 0); // duration không cần thiết khi mở khóa
        Request request = new Request(ActionType.ADMIN_UNLOCK_MEMBER, dto);

        return socketClient.sendRequestAsync(request).thenApply(response ->
                response.getStatus() == ResponseStatus.SUCCESS
        );
    }

    // 4. Lấy danh sách đấu giá
    public CompletableFuture<List<AuctionAdminDTO>> getAllAuctionsAsync() {
        Request request = new Request(ActionType.ADMIN_GET_ALL_AUCTIONS, null);

        return socketClient.sendRequestAsync(request).thenApply(response -> {
            if (response.getStatus() == ResponseStatus.SUCCESS) {
                Type listType = new TypeToken<List<AuctionAdminDTO>>(){}.getType();
                return GsonParser.GSON.fromJson(GsonParser.GSON.toJsonTree(response.getData()), listType);
            }
            return Collections.emptyList();
        });
    }

    // 5. Hủy phiên khẩn cấp
    public CompletableFuture<Boolean> forceCancelAuctionAsync(String auctionId) {
        CancelAuctionRequestDTO dto = new CancelAuctionRequestDTO(auctionId);
        Request request = new Request(ActionType.ADMIN_FORCE_CANCEL, dto);

        return socketClient.sendRequestAsync(request).thenApply(response ->
                response.getStatus() == ResponseStatus.SUCCESS
        );
    }

    // 6. Lấy tổng doanh thu
    public CompletableFuture<Double> getSystemRevenueAsync() {
        Request request = new Request(ActionType.ADMIN_GET_REVENUE, null);

        return socketClient.sendRequestAsync(request).thenApply(response -> {
            if (response.getStatus() == ResponseStatus.SUCCESS) {
                // Parse số thực từ JSON
                return GsonParser.GSON.fromJson(GsonParser.GSON.toJsonTree(response.getData()), Double.class);
            }
            return 0.0;
        });
    }
}