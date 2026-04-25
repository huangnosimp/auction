package vn.io.huangnosimp.protocol;

public enum ResponseStatus {
    SUCCESS,
    ERROR,
    UNAUTHORIZED,
    CONFLICT,
    FAILED,
    INVALID_INPUT,
    //Login
    BANNED,
    USERNAME_NOT_FOUND,
    INVALID_PASSWORD,
    //Register
    USERNAME_TAKEN,
    EMAIL_TAKEN,
    INVALID_USERNAME,
    INVALID_EMAIL,
}
