package vn.io.huangnosimp.model;

import vn.io.huangnosimp.dto.response.AuctionResponseDTO;
import java.util.concurrent.ConcurrentHashMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import vn.io.huangnosimp.enums.AuctionStatus;

public class Auction extends Entity {
    private static final Gson GSON = new Gson();
    private final ConcurrentHashMap<String, Member> bidders;
    private final Item item;

    private final Member seller;

    private String currentWinnerId;

    private double currentPrice;

    private final double startPrice;

    private final long startTime;

    private long endTime;

    private volatile AuctionStatus status;


    public Auction(
            Item item,
            Member seller,
            double startPrice,
            long startTime,
            long endTime) {
        super();
        this.item = item;
        this.seller = seller;
        this.startPrice = startPrice;
        currentPrice = startPrice;
        this.startTime = startTime;
        this.endTime = endTime;
        bidders = new ConcurrentHashMap<>();
    }
    public Auction(
            String id,
            Item item,
            Member seller,
            double startPrice,
            long startTime,
            long endTime,
            ConcurrentHashMap<String, Member> bidders) {
        super(id);
        this.item = item;
        this.seller = seller;
        this.startPrice = startPrice;
        this.startTime = startTime;
        this.endTime = endTime;
        this.bidders = bidders;
        this.setStatus(status);
    }

    public Item getItem() {
        return item;
    }

    public Member getSeller() {
        return seller;
    }

    public String getCurrentWinnerId() {
        return currentWinnerId;
    }


    public double getCurrentPrice() {
        return currentPrice;
    }


    public double getStartPrice() {
        return startPrice;
    }


    public long getStartTime() {
        return startTime;
    }

    public ConcurrentHashMap<String, Member> getBidders() {
        return bidders;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setCurrentWinnerId(String currentWinnerId) {
        this.currentWinnerId = currentWinnerId;
    }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public boolean extendEndTime(long newEndTime) {
        if (newEndTime <= this.endTime) {
            return false;
        }
        this.endTime = newEndTime;
        return true;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public void setStatusOPEN() {
        this.status = AuctionStatus.OPEN;
    }

    public void setStatusRunning() {
        if (this.status == null || this.status == AuctionStatus.OPEN) {
            this.status = AuctionStatus.RUNNING;
        }
    }

    public void setStatusFinish() {
        if (this.status == AuctionStatus.RUNNING) {
            this.status = AuctionStatus.FINISHED;
        }
    }

    public void setStatusPaid() {
        if (this.status == AuctionStatus.FINISHED) {
            this.status = AuctionStatus.PAID;
        }
    }

    public void setStatusCanceled() {
        this.status = AuctionStatus.CANCELED;
    }

    public Member getBidder(String bidderId) {
        return this.bidders.get(bidderId);
    }

    public void addBidder(Member bidder) {
        if (bidder == null || bidder.getId() == null) {
            return;
        }
        this.bidders.put(bidder.getId(), bidder);
    }

    public void removeBidder(Member bidder) {
        this.bidders.remove(bidder.getId());
    }

    public synchronized boolean placeBid(Member bidder, double amount) {
        if (bidder == null || this.status != AuctionStatus.RUNNING) {
            return false;
        }
        if (bidders.get(bidder.getId()) == null) {
            return false;
        }
        if (amount <= this.currentPrice) {
            return false;
        }
        String previousWinnerId = this.currentWinnerId;
        double previousPrice = this.currentPrice;
        if (previousWinnerId != null && previousWinnerId.equals(bidder.getId())) {
            double delta = amount - previousPrice;
            if (!bidder.freezeMoney(delta)) {
                return false;
            }
            this.addBidder(bidder);
            this.currentPrice = amount;
            return true;
        }
        if (!bidder.freezeMoney(amount)) {
            return false;
        }
        if (previousWinnerId != null) {
            Member previousWinner = this.bidders.get(previousWinnerId);
            if (previousWinner != null && !previousWinner.unfreezeMoney(previousPrice)) {
                bidder.unfreezeMoney(amount);
                return false;
            }
        }

        this.currentWinnerId = bidder.getId();
        this.currentPrice = amount;
        return true;
    }

    public boolean needExtension() {
        long timeLeft = this.endTime - System.currentTimeMillis();
        return timeLeft <= 10 * 1000;
    }

    public String biddersToJson() {
        return GSON.toJson(this.bidders);
    }

    public static ConcurrentHashMap<String, Member> biddersFromJson(String json) {
        return GSON.fromJson(json, new TypeToken<ConcurrentHashMap<String, Member>>(){}.getType());
    }

    public AuctionResponseDTO toDTO() {
        String currentWinnerUserName = null;
        if (this.currentWinnerId != null) {
            Member currentWinner = this.bidders.get(this.currentWinnerId);
            if (currentWinner != null) {
                currentWinnerUserName = currentWinner.getUsername();
            }
        }

        return new AuctionResponseDTO(
                this.bidders.size(),
                this.item.getName(),
                this.item.getDescription(),
                this.getId(),
                currentWinnerUserName,
                this.seller.getUsername(),
                this.currentPrice,
                this.startPrice,
                this.startTime,
                this.endTime,
                this.status
        );
    }
}
