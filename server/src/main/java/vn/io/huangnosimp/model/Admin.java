package vn.io.huangnosimp.model;

public class Admin extends User {

    public Admin() {
        super();
    }

    public Admin(String username, String password, String email) {
        super(username, password, email);
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
