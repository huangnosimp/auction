package vn.io.huangnosimp.dto.response;

import java.util.List;

public class DashboardResponseDTO {
    private final double balance;
    private final int joinedRooms;
    private final int winningBids;
    private final int outBids;
    private final int wonTotal;
    private final List<AuctionCardDTO> auctionCardInfo;
    private final String username;

    public DashboardResponseDTO(
            double balance,
            int activeBids,
            int winningBids,
            int outBids,
            int wonTotal,
            List<AuctionCardDTO> auctionCardInfo,
            String username) {
        this.balance = balance;
        this.joinedRooms = activeBids;
        this.winningBids = winningBids;
        this.outBids = outBids;
        this.wonTotal = wonTotal;
        this.auctionCardInfo = auctionCardInfo;
        this.username = username;
    }


    public double getBalance() {
        return balance;
    }

    public int getJoinedRooms() {
        return joinedRooms;
    }

    public int getWinningBids() {
        return winningBids;
    }

    public int getOutBids() {
        return outBids;
    }

    public int getWonTotal() {
        return wonTotal;
    }

    public List<AuctionCardDTO> getAuctionCardInfo() {
        return auctionCardInfo;
    }

    public String getUsername() {
        return username;
    }

}