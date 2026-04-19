package vn.io.huangnosimp.dto.request;

import vn.io.huangnosimp.enums.UserType;

public class LoginRequestDTO {
    private final UserType userType;
    private final String username;
    private final String password;
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
