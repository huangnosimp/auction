package vn.io.huangnosimp.network;

import vn.io.huangnosimp.model.UserType;

public class LoginRequestDTO {
    private UserType userType;
    private String username;
    private String password;
    public LoginRequestDTO(UserType userType, String userName, String password) {
        this.userType = userType;
        this.username = userName;
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public UserType getUserType() {
        return userType;
    }
}
