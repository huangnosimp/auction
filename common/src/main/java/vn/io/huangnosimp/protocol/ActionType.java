package vn.io.huangnosimp.protocol;

public enum ActionType {
    LOGIN,
    REGISTER,
    LOGOUT,
    DEPOSIT,
    WITHDRAW,

    CREATE_AUCTION,
    PLACE_BID,
    JOIN_ROOM,
    LEAVE_ROOM,
    CANCEL_AUCTION,
    BUY_NOW,

    NOTIFICATION,

    GET_DASHBOARD_INFO,
    GET_AUCTION_DETAIL,
}
