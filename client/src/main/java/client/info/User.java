package client.info;

public abstract class User {
    protected String userId;
    protected String username;
    protected String password;
    protected String email;

    public User(String userId, String username, String password) {
        this.userId = userId;
        this.username = username;
        this.password = password;

    }
    public abstract void showInfo();
}
