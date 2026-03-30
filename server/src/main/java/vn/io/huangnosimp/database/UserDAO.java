package vn.io.huangnosimp.database;

import vn.io.huangnosimp.model.Bidder;
import vn.io.huangnosimp.model.User;

/**
 * DAO interface for User persistence operations.
 * Implementation will be provided in the database layer.
 */
public interface UserDAO {
    void save(User user);
    void update(User user);
    User findById(String id);
    Bidder findBidderById(String id);
    void deleteById(String id);
}
