package vn.io.huangnosimp.factory;

import vn.io.huangnosimp.model.*;

public class UserFactory {
    public User createUser(String username, String password, String email, UserType userType) {
        return switch (userType) {
            case Admin -> new Admin(username, password, email);
            case Seller -> new Seller(username, password, email);
            case Bidder -> new Bidder(username, password, email);
        };
    }
}
