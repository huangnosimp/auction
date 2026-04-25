package vn.io.huangnosimp.service;

import vn.io.huangnosimp.dto.response.AuctionResponseDTO;
import vn.io.huangnosimp.enums.ItemType;
import vn.io.huangnosimp.network.ClientHandle;
import vn.io.huangnosimp.dto.shared.ItemAttributesDTO;

public interface IAuctionService {
    AuctionResponseDTO createAuction(String sellerId, String name, String description, ItemType type, ItemAttributesDTO attributes, double startPrice, long startTime, long endTime);
    boolean placeBid(String bidderId, String auctionId, double amount, boolean triggerAutoBid);
    boolean cancelAuction(String auctionId);
    boolean joinAuction(String userId, String auctionId, ClientHandle client);
    boolean leaveAuction(String userId, String auctionId, ClientHandle client);
    void shutdown();
}
