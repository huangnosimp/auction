package vn.io.huangnosimp.service;

import vn.io.huangnosimp.dto.response.AuctionCardDTO;
import vn.io.huangnosimp.dto.response.AuctionDetailResponseDTO;
import vn.io.huangnosimp.enums.ItemCondition;
import vn.io.huangnosimp.enums.ItemType;
import vn.io.huangnosimp.dto.response.AuctionActionResult;
import vn.io.huangnosimp.dto.response.BidResult;
import vn.io.huangnosimp.network.ClientHandle;
import vn.io.huangnosimp.dto.shared.ItemAttributesDTO;

public interface IAuctionService {
    AuctionCardDTO createAuction(String sellerId, String name, String description, ItemType type,
                                 ItemAttributesDTO attributes, double startPrice, long startTime,
                                 long endTime, ItemCondition condition, double minimumIncrement,
                                 double buyNowPrice);
    BidResult placeBid(String bidderId, String auctionId, double amount, boolean triggerAutoBid);
    AuctionActionResult cancelAuction(String auctionId);
    BidResult buyNow(String userId, String auctionId);
    AuctionActionResult joinAuction(String userId, String auctionId, ClientHandle client);
    AuctionActionResult leaveAuction(String userId, String auctionId, ClientHandle client);
    void shutdown();
}
