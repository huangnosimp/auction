package vn.io.huangnosimp.dto.request;

import vn.io.huangnosimp.enums.UserType;

public class RegisterRequestDTO {
    private final UserType userType;
    private final String username;
    private final String password;
    private final String email;

    public RegisterRequestDTO(UserType userType, String username, String password, String email) {
        this.userType = userType;
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public UserType getUserType() {
        return userType;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }
}
