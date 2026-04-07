package vn.io.huangnosimp.database;

public interface UserDAO {
    void saveUserInfoToDatabase(String UUID, String userName, String password, String email);
    String getUserUUID(String username, String password);
}
