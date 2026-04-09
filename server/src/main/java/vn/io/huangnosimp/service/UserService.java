package vn.io.huangnosimp.service;

import vn.io.huangnosimp.database.UserDAO;
import vn.io.huangnosimp.model.*;
import vn.io.huangnosimp.network.ClientHandle;

import java.util.concurrent.ConcurrentHashMap;

//prototype
public class UserService {
    private static volatile UserService instance;
    private ConcurrentHashMap<String, Bidder> onlineBidders;
    private ConcurrentHashMap<String, Seller> onlineSellers;
    private ConcurrentHashMap<String, Admin> onlineAdmin;
    private UserService() {
        this.onlineBidders = new ConcurrentHashMap<>();
        this.onlineSellers = new ConcurrentHashMap<>();
        this.onlineAdmin = new ConcurrentHashMap<>();
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

    public boolean register(String userName, String password, String email) {
        if (userName == null || password == null || email == null) {
            return false;
        }

        return true;
    }
    public boolean Login(UserType type, String username, String password, ClientHandle client) {
        if (username == null || password == null) {
            return false;
        }
        if (type == UserType.Bidder) {
            Bidder bidder = new Bidder(username, password);
            onlineBidders.put(bidder.getId(), bidder);
            client.setUserId(bidder.getId());
            client.setUserType(type);
            return true;
        } else if (type == UserType.Seller) {
            Seller seller = new Seller(username, password);
            onlineSellers.put(seller.getId(), seller);
            client.setUserId(seller.getId());
            client.setUserType(type);
            return true;
        } else if (type == UserType.Admin) {
            Admin admin = new Admin(username, password);
            onlineAdmin.put(admin.getId(), admin);
            client.setUserId(admin.getId());
            client.setUserType(type);
            return true;
        } else {
            return false;
        }
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
