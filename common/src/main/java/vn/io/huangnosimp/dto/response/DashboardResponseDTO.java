package vn.io.huangnosimp.dto.response;

import java.util.List;

public class DashboardResponseDTO {
    private double balance;
    private int joinedRooms;
    private int winningBids;
    private int outBids;
    private int wonTotal;
    private List<AuctionCardDTO> auctionCardInfo;

    public DashboardResponseDTO(
            double balance,
            int activeBids,
            int winningBids,
            int outBids,
            int wonTotal,
            List<AuctionCardDTO> auctionCardInfo) {
        this.balance = balance;
        this.joinedRooms = activeBids;
        this.winningBids = winningBids;
        this.outBids = outBids;
        this.wonTotal = wonTotal;
        this.auctionCardInfo = auctionCardInfo;
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

}