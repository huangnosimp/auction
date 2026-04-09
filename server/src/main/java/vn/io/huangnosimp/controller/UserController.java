package vn.io.huangnosimp.controller;

import vn.io.huangnosimp.model.ResponseStatus;
import vn.io.huangnosimp.model.UserType;
import vn.io.huangnosimp.network.*;
import vn.io.huangnosimp.service.UserService;

public class UserController {
    public Response handleLogin(Request request, ClientHandle client) {
        String userData = Request.GSON.toJson(request.getData());
        LoginRequestDTO loginRequestDTO = Request.GSON.fromJson(userData, LoginRequestDTO.class);
        String username = loginRequestDTO.getUsername();
        String password = loginRequestDTO.getPassword();
        UserType userType = loginRequestDTO.getUserType();
        if (UserService.getInstance().Login(userType, username, password, client)) {
            Response response = new Response(ResponseStatus.SUCCESS, "Login successful!");
            return response;
        } else {
            Response response = new Response(ResponseStatus.ERROR, "Login failed! Please check your credentials.");
            return response;
        }
    }
    public Message handleRegister(Message request, ClientHandle client) {
        System.out.println("gọi đến UserService");
        return null;
    }
}
