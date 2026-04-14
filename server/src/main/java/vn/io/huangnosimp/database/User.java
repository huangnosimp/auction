package vn.io.huangnosimp.database;

public interface User {
    void saveUser();
    String getUserUUID();
    String getPassword();
    String getUsername();
    boolean checkUsername(String username);
}
