package vn.io.huangnosimp.dto.response;

import java.util.List;

public class DashboardResponseDTO {
    private double balance;
    private int activeBids;
    private int winningBids;
    private int outBids;
    private int wonTotal;
    private List<AuctionCardDTO> auctionCardInfolist;

    public DashboardResponseDTO(
            double balance,
            int activeBids,
            int winningBids,

            int outBids,
            int wonTotal,
            List<AuctionCardDTO> auctionCardInfo) {
        this.balance = balance;
        this.activeBids = activeBids;
        this.winningBids = winningBids;
        this.outBids = outBids;
        this.wonTotal = wonTotal;
        this.auctionCardInfolist = auctionCardInfo;
    }


    public double getBalance() {
        return balance;
    }

    public int getActiveBids() {
        return activeBids;
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
        return auctionCardInfolist;
    }

}