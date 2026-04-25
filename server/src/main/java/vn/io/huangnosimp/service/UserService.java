package vn.io.huangnosimp.service;

import org.mindrot.jbcrypt.BCrypt;

import vn.io.huangnosimp.dto.response.LoginResult;
import vn.io.huangnosimp.dto.response.RegisterResult;
import vn.io.huangnosimp.enums.UserType;
import vn.io.huangnosimp.model.*;
import vn.io.huangnosimp.network.ClientHandle;
import vn.io.huangnosimp.repository.IUserRepository;

import java.util.UUID;

public class UserService implements IUserService {
    private final IUserRepository userRepository;
    public UserService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Member getMember(String memberId) {
        User user = userRepository.findById(memberId);
        if (user instanceof Member) {
            return (Member) user;
        }
        return null;
    }

    @Override
    public RegisterResult register(UserType type, String username, String password, String email) {
        if (userRepository.checkUsername(username)) {
            return RegisterResult.USERNAME_TAKEN;
        }
        if (userRepository.checkEmail(email)) {
            return RegisterResult.EMAIL_TAKEN;
        }
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        String id = UUID.randomUUID().toString();
        if (userRepository.saveUser(id, username, hashedPassword, email, type.name())) {
            return RegisterResult.SUCCESS;
        }
        return RegisterResult.ERROR;
    }

    @Override
    public LoginResult login(UserType type, String username, String password, ClientHandle client) {
        User user = userRepository.findByUsername(username);
        if (user == null || !user.getClass().getSimpleName().equalsIgnoreCase(type.name())) {
            return LoginResult.USER_NOT_FOUND;
        }
        if (BCrypt.checkpw(password, user.getPassword())) {
            client.setUserId(user.getId());
            client.setUserType(type);
            return LoginResult.SUCCESS;
        }
        return LoginResult.INVALID_PASSWORD;
    }

    @Override
    public synchronized boolean deposit(String userId, double amount) {
        User user = userRepository.findById(userId);
        if (user instanceof Member member && amount > 0) {
            double newBalance = member.getAccountBalance() + amount;
            return userRepository.updateBalance(userId, newBalance);
        }
        return false;
    }

    @Override
    public synchronized boolean withdraw(String userId, double amount) {
        User user = userRepository.findById(userId);
        if (user instanceof Member member && amount > 0) {
            if (member.getAccountBalance() < amount) {
                return false;
            }
            double newBalance = member.getAccountBalance() - amount;
            return userRepository.updateBalance(userId, newBalance);
        }
        return false;
    }

    @Override
    public boolean updateBalance(String userId, double newBalance) {
        return userRepository.updateBalance(userId, newBalance);
    }

    @Override
    public boolean updateFrozenBalance(String userId, double newFrozenBalance) {
        return userRepository.updateFrozenBalance(userId, newFrozenBalance);
    }

    @Override
    public boolean banUser(String userId) {
        return false;
    }

    @Override
    public boolean unbanUser(String userId) {
        return false;
    }
}
