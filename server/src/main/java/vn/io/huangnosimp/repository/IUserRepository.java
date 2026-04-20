package vn.io.huangnosimp.repository;

import vn.io.huangnosimp.model.User;

public interface IUserRepository {
    boolean checkUsername(String username);
    boolean checkEmail(String email);
    boolean saveUser(String userId, String username, String password, String email, String role);
    User findByUsername(String username);
    User findById(String userId);
    boolean updateBalance(String userId, double newBalance);
    boolean banUser(String userId);
    boolean unbanUser(String userId);
}
