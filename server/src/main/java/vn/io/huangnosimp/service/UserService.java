package vn.io.huangnosimp.service;

import org.mindrot.jbcrypt.BCrypt;

import vn.io.huangnosimp.dto.response.LoginResult;
import vn.io.huangnosimp.dto.response.RegisterResult;
import vn.io.huangnosimp.enums.UserType;
import vn.io.huangnosimp.model.*;
import vn.io.huangnosimp.network.ClientHandle;
import vn.io.huangnosimp.repository.IUserRepository;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserService implements IUserService {
    private final IUserRepository userRepository;
    private static final String EMAIL_REGEX = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
    private static final Pattern pattern = Pattern.compile(EMAIL_REGEX);
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
        if (!isValidInput(type, username, password) || email == null || email.isBlank()) {
            return RegisterResult.INVALID_INPUT;
        }
        if (userRepository.checkUsername(username)) {
            return RegisterResult.USERNAME_TAKEN;
        }
        Matcher matcher = pattern.matcher(email);
        if (!matcher.matches()) {
            return RegisterResult.INVALID_EMAIL;
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
        if (!isValidInput(type, username, password)) {
            return LoginResult.INVALID_INPUT;
        }
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

    private boolean isValidInput(UserType type, String username, String password) {
        return type != null && username != null && !username.isBlank() && password != null && !password.isBlank();
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
