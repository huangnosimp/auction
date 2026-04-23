package vn.io.huangnosimp.service;

import vn.io.huangnosimp.dto.response.LoginResult;
import vn.io.huangnosimp.model.Member;
import vn.io.huangnosimp.dto.response.RegisterResult;
import vn.io.huangnosimp.enums.UserType;
import vn.io.huangnosimp.network.ClientHandle;

public interface IUserService {
    Member getMember(String memberId);
    RegisterResult register(UserType type, String username, String password, String email);
    LoginResult login(UserType type, String username, String password, ClientHandle client);
    boolean deposit(String userId, double amount);
    boolean withdraw(String userId, double amount);
    boolean updateBalance(String userId, double newBalance);
    boolean updateFrozenBalance(String userId, double newFrozenBalance);
    boolean banUser(String userId);
    boolean unbanUser(String userId);
}
