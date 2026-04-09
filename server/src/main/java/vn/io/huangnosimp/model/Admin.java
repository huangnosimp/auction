package vn.io.huangnosimp.model;

public class Admin extends User {

    public Admin() {
        super();
    }

    public Admin(String username, String password) {
        super(username, password, null);
    }

    @Override
    public String toString() {
        return "Admin{" +
                "id='" + getId() + '\'' +
                ", username='" + getUsername() + '\'' +
                ", email='" + getEmail() + '\'' +
                '}';
    }
}
