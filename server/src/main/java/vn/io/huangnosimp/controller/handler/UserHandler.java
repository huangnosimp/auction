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
import vn.io.huangnosimp.protocol.ResponseStatus;
import vn.io.huangnosimp.network.*;
import vn.io.huangnosimp.service.IUserService;

public class UserHandler {
    public static class LoginHandler implements RequestHandler {
        private final IUserService userService;
        public LoginHandler(IUserService userService) {
            this.userService = userService;
        }
        @Override
        public Response handle(Request request, ClientHandle client) {
            LoginRequestDTO loginRequestDTO = GsonParser.GSON.fromJson(GsonParser.GSON.toJson(request.getData()), LoginRequestDTO.class);
            LoginResult result = userService.login(loginRequestDTO.getUserType(), loginRequestDTO.getUsername(), loginRequestDTO.getPassword(), client);
            if (result == LoginResult.SUCCESS) {
                return new Response(ResponseStatus.SUCCESS, "Login successful");
            } else if (result == LoginResult.USER_NOT_FOUND) {
                return new Response(ResponseStatus.UNAUTHORIZED, "Invalid username");
            } else if (result == LoginResult.INVALID_PASSWORD) {
                return new Response(ResponseStatus.UNAUTHORIZED, "Invalid password");
            } else if (result == LoginResult.BANNED) {
                return new Response(ResponseStatus.FORBIDDEN, "Your account has been banned. Please contact admin.");
            } else {
                return new Response(ResponseStatus.FAILED, "Login failed");
            }
        }
    }
    public static class LogoutHandler implements RequestHandler {
        @Override
        public Response handle(Request request, ClientHandle client) {
            client.setUserId(null);
            client.setUserType(null);
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
            if (result == RegisterResult.SUCCESS) {
                return new Response(ResponseStatus.SUCCESS, "Registration successful");
            } else if (result == RegisterResult.USERNAME_TAKEN) {
                return new Response(ResponseStatus.CONFLICT, "Username already taken");
            } else if (result == RegisterResult.EMAIL_TAKEN) {
                return new Response(ResponseStatus.CONFLICT, "Email already taken");
            }else {
                return new Response(ResponseStatus.FAILED, "Register failed");
            }
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
            boolean success = userService.deposit(client.getUserId(), dto.getAmount());
            if (success) {
                return new Response(ResponseStatus.SUCCESS, "Deposit successful");
            } else {
                return new Response(ResponseStatus.FAILED, "Deposit failed");
            }
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
            boolean success = userService.withdraw(client.getUserId(), dto.getAmount());
            if (success) {
                return new Response(ResponseStatus.SUCCESS, "Withdraw successful");
            } else {
                return new Response(ResponseStatus.FAILED, "Withdraw failed");
            }
        }
    }
}
