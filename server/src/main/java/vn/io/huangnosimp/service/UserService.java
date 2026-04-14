package vn.io.huangnosimp.service;

import vn.io.huangnosimp.database.User;
import vn.io.huangnosimp.factory.UserFactory;
import vn.io.huangnosimp.model.*;
import vn.io.huangnosimp.network.ClientHandle;

import java.util.concurrent.ConcurrentHashMap;

//prototype
public class UserService {
    private static volatile UserService instance;
    private ConcurrentHashMap<String, Bidder> onlineBidders;
    private ConcurrentHashMap<String, Seller> onlineSellers;
    private ConcurrentHashMap<String, Admin> onlineAdmin;
    private UserFactory userFactory;
    private UserService() {
        this.onlineBidders = new ConcurrentHashMap<>();
        this.onlineSellers = new ConcurrentHashMap<>();
        this.onlineAdmin = new ConcurrentHashMap<>();
        this.userFactory = new UserFactory();
    }
    public static UserService getInstance() {
        if (instance == null) {
            synchronized (UserService.class) {
                if (instance == null) {
                    instance = new UserService();
                }
            }
        }
        return instance;
    }

    public Bidder getBidder(String bidderId) {
        return new Bidder(bidderId, "password", bidderId + "@example.com", 1000.0);
    }

    public Seller getSeller(String sellerId) {
        return new Seller(sellerId, "password", sellerId + "@example.com");
    }

    public String getBidderUserName(String bidderId) {
        return bidderId;
    }

    public boolean register(UserType type, String username, String password, String email) {
        if (username == null || password == null || email == null) {
            return false;
        }
        userFactory.createUser(username, password, email, type);

        return true;
    }
    public boolean Login(UserType type, String username, String password, String email, ClientHandle client) {

    }

    public boolean joinAuction(String auctionId, String bidderId) {
        Auction auction = AuctionService.getInstance().getAuctions().get(auctionId);
        Bidder bidder = this.getBidder(bidderId);
        if (auction == null || bidder == null || auction.getStatus() != AuctionStatus.OPEN || auction.getStatus() != AuctionStatus.RUNNING) {
            return false;
        }
        bidder.joinRoom(auctionId);
        return true;
    }

    public boolean leaveAuction(String auctionId, String bidderId) {
        Auction auction = AuctionService.getInstance().getAuctions().get(auctionId);
        Bidder bidder = this.getBidder(bidderId);
        if (auction == null || bidder == null ) {
            return false;
        }
        bidder.leaveRoom(auctionId);
        return true;
    }
}
