package vn.io.huangnosimp.dto.response;

public enum BidResult {
    SUCCESS,
    AUCTION_NOT_FOUND,
    AUCTION_ENDED,
    INSUFFICIENT_FUNDS,
    BID_TOO_LOW,
    ALREADY_HIGHEST_BIDDER,
    NOT_IN_ROOM,
    ERROR
}
