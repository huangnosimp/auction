package vn.io.huangnosimp.service;

import vn.io.huangnosimp.database.AutoBidDAO;
import vn.io.huangnosimp.database.BidDAO;
import vn.io.huangnosimp.model.Auction;
import vn.io.huangnosimp.model.AutoBidConfig;
import vn.io.huangnosimp.model.BidTransaction;
import vn.io.huangnosimp.model.Bidder;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AutoBidService {

    private final AutoBidDAO autoBidDAO;
    private final BidDAO bidDAO;
    private final NotificationService notificationService;
    private final AuctionService auctionService;

    public AutoBidService(AutoBidDAO autoBidDAO,
                          BidDAO bidDAO,
                          NotificationService notificationService,
                          AuctionService auctionService) {
        this.autoBidDAO = autoBidDAO;
        this.bidDAO = bidDAO;
        this.notificationService = notificationService;
        this.auctionService = auctionService;
    }


    public void processAutoBids(Auction auction, BidTransaction latestBid) {
        List<AutoBidConfig> allConfigs = autoBidDAO.findByAuctionId(auction.getId());

        if (allConfigs == null || allConfigs.isEmpty()) {
            return;
        }

        double currentPrice = auction.getCurrentPrice();

        List<AutoBidConfig> eligible = allConfigs.stream()
                .filter(config -> !config.getBidder().getId().equals(latestBid.getBidder().getId()))
                .filter(config -> config.getMaxBid() > currentPrice)
                .sorted(Comparator
                        .comparingDouble(AutoBidConfig::getMaxBid).reversed()
                        .thenComparing(AutoBidConfig::getRegisteredAt))
                .collect(Collectors.toList());

        if (eligible.isEmpty()) {
            System.out.println("[AutoBidService] No eligible auto-bid configs for auction " +
                    auction.getId());
            return;
        }

        AutoBidConfig winner = eligible.get(0);
        Bidder autoBidder = winner.getBidder();

        double counterAmount = Math.min(
                currentPrice + winner.getIncrement(),
                winner.getMaxBid()
        );

        if (counterAmount <= currentPrice) {
            System.out.println("[AutoBidService] Counter-bid amount $" + counterAmount +
                    " does not exceed current price $" + currentPrice + " — skipping");
            return;
        }

        if (eligible.size() >= 2) {
            AutoBidConfig runnerUp = eligible.get(1);

            if (Double.compare(runnerUp.getMaxBid(), winner.getMaxBid()) == 0) {
                counterAmount = winner.getMaxBid();
            } else if (runnerUp.getMaxBid() > currentPrice) {
                double minToBeatRunnerUp = runnerUp.getMaxBid() + winner.getIncrement();
                counterAmount = Math.min(minToBeatRunnerUp, winner.getMaxBid());

                if (counterAmount <= currentPrice) {
                    counterAmount = Math.min(currentPrice + winner.getIncrement(), winner.getMaxBid());
                }
            }
        }

        BidTransaction autoBidTx = new BidTransaction(
                auction,
                autoBidder,
                counterAmount,
                LocalDateTime.now(),
                true
        );

        auction.setCurrentPrice(counterAmount);
        auction.setCurrentWinner(autoBidder);
        auction.setUpdatedAt(LocalDateTime.now());

        bidDAO.save(autoBidTx);

        System.out.println("[AutoBidService] AUTO-BID placed — Bidder: " +
                autoBidder.getUsername() + ", Amount: $" + counterAmount +
                ", Auction: " + auction.getId());

        notificationService.notifyNewBid(auction, autoBidTx);

        auctionService.checkAndExtendAuction(auction);
    }
}
