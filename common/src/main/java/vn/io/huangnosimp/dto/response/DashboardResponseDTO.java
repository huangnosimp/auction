package vn.io.huangnosimp.dto.response;

public class DashboardResponseDTO {
    private double balance;
    private int activeBids;
    private int winningBids;
    private int outBids;
    private int wonTotal;
    private AuctionCardDTO auctionCardInfo;

    public DashboardResponseDTO(
            double balance,
            int activeBids,
            int winningBids,

            int outBids,
            int wonTotal,
            AuctionCardDTO auctionCardInfo) {
        this.balance = balance;
        this.activeBids = activeBids;
        this.winningBids = winningBids;
        this.outBids = outBids;
        this.wonTotal = wonTotal;
        this.auctionCardInfo = auctionCardInfo;
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

    public AuctionCardDTO getAuctionCardInfo() {
        return auctionCardInfo;
    }

}