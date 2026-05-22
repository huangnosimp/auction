package vn.io.huangnosimp.service;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
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
        logger.debug("Member lookup returned no member userId={}", memberId);
        return null;
    }

    @Override
    public RegisterResult register(UserType type, String username, String password, String email) {
        if (!isValidInput(type, username, password) || email == null || email.isBlank()) {
            logger.warn("Registration rejected due to invalid input userType={} usernamePresent={} emailPresent={}",
                    type, username != null && !username.isBlank(), email != null && !email.isBlank());
            return RegisterResult.INVALID_INPUT;
        }
        if (userRepository.checkUsername(username)) {
            logger.info("Registration rejected because username is taken username={}", username);
            return RegisterResult.USERNAME_TAKEN;
        }
        Matcher matcher = pattern.matcher(email);
        if (!matcher.matches()) {
            logger.info("Registration rejected because email is invalid username={}", username);
            return RegisterResult.INVALID_EMAIL;
        }
        if (userRepository.checkEmail(email)) {
            logger.info("Registration rejected because email is taken username={}", username);
            return RegisterResult.EMAIL_TAKEN;
        }
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        String id = UUID.randomUUID().toString();
        if (userRepository.saveUser(id, username, hashedPassword, email, type.name())) {
            logger.info("User registered userId={} userType={}", id, type);
            return RegisterResult.SUCCESS;
        }
        logger.error("Registration failed while saving user username={} userType={}", username, type);
        return RegisterResult.ERROR;
    }

    @Override
    public LoginResult login(UserType type, String username, String password, ClientHandle client) {
        if (!isValidInput(type, username, password)) {
            logger.warn("Login rejected due to invalid input userType={} usernamePresent={}",
                    type, username != null && !username.isBlank());
            return LoginResult.INVALID_INPUT;
        }
        User user = userRepository.findByUsername(username);

        if (user == null || !user.getClass().getSimpleName().equalsIgnoreCase(type.name())) {
            logger.info("Login rejected because user was not found username={} userType={}", username, type);
            return LoginResult.USER_NOT_FOUND;
        }

        if (!BCrypt.checkpw(password, user.getPassword())) {
            logger.info("Login rejected because password is invalid userId={} userType={}", user.getId(), type);
            return LoginResult.INVALID_PASSWORD;
        }

        // Lazy unban
        if (user instanceof Member member && member.isBanned()) {
            LocalDateTime banUntil = member.getBanUntil();

            if (banUntil != null && LocalDateTime.now().isAfter(banUntil)) {
                member.setBanned(false);
                member.setBanUntil(null);

                userRepository.updateBanStatus(member.getId(), false, null);
                logger.info("Expired ban cleared during login userId={}", member.getId());
            } else {
                client.setUserId(null);
                client.setUserType(null);
                logger.info("Login rejected because member is banned userId={} banUntil={}", member.getId(), banUntil);
                return LoginResult.BANNED;
            }
        }

        client.setUserId(user.getId());
        client.setUserType(type);
        logger.info("Login successful userId={} userType={}", user.getId(), type);
        return LoginResult.SUCCESS;
    }

    private boolean isValidInput(UserType type, String username, String password) {
        return type != null && username != null && !username.isBlank() && password != null && !password.isBlank();
    }

    @Override
    public synchronized TransactionResult deposit(String userId, double amount) {
        if (amount <= 0) {
            logger.warn("Deposit rejected due to invalid amount userId={} amount={}", userId, amount);
            return TransactionResult.INVALID_AMOUNT;
        }
        User user = userRepository.findById(userId);
        if (user instanceof Member member) {
            double newBalance = member.getAccountBalance() + amount;
            if (userRepository.updateBalance(userId, newBalance)) {
                logger.info("Deposit successful userId={} amount={}", userId, amount);
                return TransactionResult.SUCCESS;
            }
            logger.error("Deposit failed while updating balance userId={} amount={}", userId, amount);
            return TransactionResult.ERROR;
        }
        logger.warn("Deposit rejected because user was not found userId={}", userId);
        return TransactionResult.USER_NOT_FOUND;
    }

    @Override
    public synchronized TransactionResult withdraw(String userId, double amount) {
        if (amount <= 0) {
            logger.warn("Withdraw rejected due to invalid amount userId={} amount={}", userId, amount);
            return TransactionResult.INVALID_AMOUNT;
        }
        User user = userRepository.findById(userId);
        if (user instanceof Member member) {
            if (member.getAccountBalance() < amount) {
                logger.info("Withdraw rejected due to insufficient funds userId={} amount={}", userId, amount);
                return TransactionResult.INSUFFICIENT_FUNDS;
            }
            double newBalance = member.getAccountBalance() - amount;
            if (userRepository.updateBalance(userId, newBalance)) {
                logger.info("Withdraw successful userId={} amount={}", userId, amount);
                return TransactionResult.SUCCESS;
            }
            logger.error("Withdraw failed while updating balance userId={} amount={}", userId, amount);
            return TransactionResult.ERROR;
        }
        logger.warn("Withdraw rejected because user was not found userId={}", userId);
        return TransactionResult.USER_NOT_FOUND;
    }

    @Override
    public TransactionResult updateBalance(String userId, double newBalance) {
        if (newBalance < 0) {
            logger.warn("Balance update rejected due to invalid amount userId={} newBalance={}", userId, newBalance);
            return TransactionResult.INVALID_AMOUNT;
        }
        if (userRepository.updateBalance(userId, newBalance)) {
            logger.debug("Balance updated userId={}", userId);
            return TransactionResult.SUCCESS;
        }
        logger.error("Balance update failed userId={}", userId);
        return TransactionResult.ERROR;
    }

    @Override
    public TransactionResult updateFrozenBalance(String userId, double newFrozenBalance) {
        if (newFrozenBalance < 0) {
            logger.warn("Frozen balance update rejected due to invalid amount userId={} newFrozenBalance={}",
                    userId, newFrozenBalance);
            return TransactionResult.INVALID_AMOUNT;
        }
        if (userRepository.updateFrozenBalance(userId, newFrozenBalance)) {
            logger.debug("Frozen balance updated userId={}", userId);
            return TransactionResult.SUCCESS;
        }
        logger.error("Frozen balance update failed userId={}", userId);
        return TransactionResult.ERROR;
    }
}
