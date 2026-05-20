package vn.io.huangnosimp.controller.handler;

import vn.io.huangnosimp.dto.request.DepositWithdrawRequestDTO;
import vn.io.huangnosimp.dto.request.LoginRequestDTO;
import vn.io.huangnosimp.dto.request.RegisterRequestDTO;
import vn.io.huangnosimp.util.GsonParser;
import vn.io.huangnosimp.protocol.Request;
import vn.io.huangnosimp.protocol.Response;
import vn.io.huangnosimp.controller.RequestHandler;
import vn.io.huangnosimp.dto.response.LoginResult;
import vn.io.huangnosimp.dto.response.RegisterResult;
import vn.io.huangnosimp.dto.response.TransactionResult;
import vn.io.huangnosimp.protocol.ResponseStatus;
import vn.io.huangnosimp.network.*;
import vn.io.huangnosimp.service.IUserService;
import vn.io.huangnosimp.util.SensitiveDataMasker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserHandler {
    private static final Logger logger = LoggerFactory.getLogger(UserHandler.class);

    public static class LoginHandler implements RequestHandler {
        private final IUserService userService;
        public LoginHandler(IUserService userService) {
            this.userService = userService;
        }
        @Override
        public Response handle(Request request, ClientHandle client) {
            LoginRequestDTO loginRequestDTO = GsonParser.GSON.fromJson(GsonParser.GSON.toJson(request.getData()), LoginRequestDTO.class);
            LoginResult result = userService.login(loginRequestDTO.getUserType(), loginRequestDTO.getUsername(), loginRequestDTO.getPassword(), client);
            logger.info("Login attempted username={} userType={} result={}", loginRequestDTO.getUsername(), loginRequestDTO.getUserType(), result);
            return switch (result) {
                case SUCCESS -> new Response(ResponseStatus.SUCCESS, "Login successful");
                case USER_NOT_FOUND -> new Response(ResponseStatus.FAILED, "Invalid username");
                case INVALID_PASSWORD -> new Response(ResponseStatus.FAILED, "Invalid password");
                case BANNED -> new Response(ResponseStatus.FORBIDDEN, "Your account has been banned. Please contact admin.");
                default -> new Response(ResponseStatus.FAILED, "Login failed");
            };
        }
    }
    public static class LogoutHandler implements RequestHandler {
        @Override
        public Response handle(Request request, ClientHandle client) {
            String userId = client.getUserId();
            client.setUserId(null);
            client.setUserType(null);
            logger.info("Logout successful userId={}", userId);
            return new Response(ResponseStatus.SUCCESS, "Logout successful");
        }
    }
    public static class RegisterHandler implements RequestHandler {
        private final IUserService userService;
        public RegisterHandler(IUserService userService) {
            this.userService = userService;
        }
        @Override
        public Response handle(Request request, ClientHandle client) {
            RegisterRequestDTO dto = GsonParser.GSON.fromJson(GsonParser.GSON.toJsonTree(request.getData()), RegisterRequestDTO.class);
            RegisterResult result = userService.register(dto.getUserType(), dto.getUsername(), dto.getPassword(), dto.getEmail());
            logger.info(
                    "Register attempted username={} email={} userType={} result={}",
                    dto.getUsername(), SensitiveDataMasker.maskEmail(dto.getEmail()), dto.getUserType(), result
            );
            return switch (result) {
                case SUCCESS -> new Response(ResponseStatus.SUCCESS, "Registration successful");
                case USERNAME_TAKEN -> new Response(ResponseStatus.FAILED, "Username already taken");
                case EMAIL_TAKEN -> new Response(ResponseStatus.FAILED, "Email already taken");
                default -> new Response(ResponseStatus.FAILED, "Register failed");
            };
        }
    }
    public static class DepositHandler implements RequestHandler {
        private final IUserService userService;
        public DepositHandler(IUserService userService) {
            this.userService = userService;
        }

        @Override
        public Response handle(Request request, ClientHandle client) {
            DepositWithdrawRequestDTO dto = GsonParser.GSON.fromJson(GsonParser.GSON.toJsonTree(request.getData()), DepositWithdrawRequestDTO.class);
            TransactionResult result = userService.deposit(client.getUserId(), dto.getAmount());
            logger.info("Deposit attempted userId={} amount={} result={}", client.getUserId(), dto.getAmount(), result);
            return switch (result) {
                case SUCCESS -> new Response(ResponseStatus.SUCCESS, "Deposit successful");
                case INVALID_AMOUNT -> new Response(ResponseStatus.FAILED, "Invalid deposit amount");
                case USER_NOT_FOUND -> new Response(ResponseStatus.FAILED, "User not found");
                default -> new Response(ResponseStatus.FAILED, "Deposit failed");
            };
        }
    }
    public static class WithdrawHandler implements RequestHandler {
        private final IUserService userService;
        public WithdrawHandler(IUserService userService) {
            this.userService = userService;
        }

        @Override
        public Response handle(Request request, ClientHandle client) {
            DepositWithdrawRequestDTO dto = GsonParser.GSON.fromJson(GsonParser.GSON.toJsonTree(request.getData()), DepositWithdrawRequestDTO.class);
            TransactionResult result = userService.withdraw(client.getUserId(), dto.getAmount());
            logger.info("Withdraw attempted userId={} amount={} result={}", client.getUserId(), dto.getAmount(), result);
            return switch (result) {
                case SUCCESS -> new Response(ResponseStatus.SUCCESS, "Withdraw successful");
                case INVALID_AMOUNT -> new Response(ResponseStatus.FAILED, "Invalid withdraw amount");
                case INSUFFICIENT_FUNDS -> new Response(ResponseStatus.FAILED, "Insufficient funds");
                case USER_NOT_FOUND -> new Response(ResponseStatus.FAILED, "User not found");
                default -> new Response(ResponseStatus.FAILED, "Withdraw failed");
            };
        }
    }
}
