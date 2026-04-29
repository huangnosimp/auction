package vn.io.huangnosimp.repository;

import vn.io.huangnosimp.model.User;

import java.util.List;

public interface IUserRepository {
    boolean checkUsername(String username);
    boolean checkEmail(String email);
    boolean saveUser(String userId, String username, String password, String email, String role);
    User findByUsername(String username);
    User findById(String userId);
    boolean updateBalance(String userId, double newBalance);
    boolean updateFrozenBalance(String userId, double newFrozenBalance);
    boolean updateStatus(String userId, boolean isBanned);
    List<User> findAll();
}
