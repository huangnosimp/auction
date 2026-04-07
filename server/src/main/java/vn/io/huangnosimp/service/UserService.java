package vn.io.huangnosimp.service;

import vn.io.huangnosimp.database.UserDAO;
import vn.io.huangnosimp.model.Bidder;
import vn.io.huangnosimp.model.Seller;

//prototype
public class UserService {
    private static volatile UserService instance;

    private UserService() {

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
}
