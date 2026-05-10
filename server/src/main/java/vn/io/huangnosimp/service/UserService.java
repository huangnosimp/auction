package vn.io.huangnosimp.service;

import org.mindrot.jbcrypt.BCrypt;

import vn.io.huangnosimp.dto.response.LoginResult;
import vn.io.huangnosimp.dto.response.RegisterResult;
import vn.io.huangnosimp.dto.response.TransactionResult;
import vn.io.huangnosimp.enums.UserType;
import vn.io.huangnosimp.model.*;
import vn.io.huangnosimp.network.ClientHandle;
import vn.io.huangnosimp.repository.IUserRepository;

import java.time.LocalDateTime;
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

        if (!BCrypt.checkpw(password, user.getPassword())) {
            return LoginResult.INVALID_PASSWORD;
        }

        // Lazy unban
        if (user instanceof Member member && member.isBanned()) {
            LocalDateTime banUntil = member.getBanUntil();

            if (banUntil != null && LocalDateTime.now().isAfter(banUntil)) {
                member.setBanned(false);
                member.setBanUntil(null);

                userRepository.updateBanStatus(member.getId(), false, null);
            } else {
                client.setUserId(null);
                client.setUserType(null);
                return LoginResult.BANNED;
            }
        }

        client.setUserId(user.getId());
        client.setUserType(type);
        return LoginResult.SUCCESS;
    }

    private boolean isValidInput(UserType type, String username, String password) {
        return type != null && username != null && !username.isBlank() && password != null && !password.isBlank();
    }

    @Override
    public synchronized TransactionResult deposit(String userId, double amount) {
        if (amount <= 0) return TransactionResult.INVALID_AMOUNT;
        User user = userRepository.findById(userId);
        if (user instanceof Member member) {
            double newBalance = member.getAccountBalance() + amount;
            if (userRepository.updateBalance(userId, newBalance)) {
                return TransactionResult.SUCCESS;
            }
            return TransactionResult.ERROR;
        }
        return TransactionResult.USER_NOT_FOUND;
    }

    @Override
    public synchronized TransactionResult withdraw(String userId, double amount) {
        if (amount <= 0) return TransactionResult.INVALID_AMOUNT;
        User user = userRepository.findById(userId);
        if (user instanceof Member member) {
            if (member.getAccountBalance() < amount) {
                return TransactionResult.INSUFFICIENT_FUNDS;
            }
            double newBalance = member.getAccountBalance() - amount;
            if (userRepository.updateBalance(userId, newBalance)) {
                return TransactionResult.SUCCESS;
            }
            return TransactionResult.ERROR;
        }
        return TransactionResult.USER_NOT_FOUND;
    }

    @Override
    public TransactionResult updateBalance(String userId, double newBalance) {
        if (newBalance < 0) return TransactionResult.INVALID_AMOUNT;
        if (userRepository.updateBalance(userId, newBalance)) {
            return TransactionResult.SUCCESS;
        }
        return TransactionResult.ERROR;
    }

    @Override
    public TransactionResult updateFrozenBalance(String userId, double newFrozenBalance) {
        if (newFrozenBalance < 0) return TransactionResult.INVALID_AMOUNT;
        if (userRepository.updateFrozenBalance(userId, newFrozenBalance)) {
            return TransactionResult.SUCCESS;
        }
        return TransactionResult.ERROR;
    }
}
