package client.info;

public class Client extends User {
    private double balance;

    public Client(String username, String fullName, String email, double balance) {
        super(username, fullName, email);
        this.balance = balance;
    }

    public void showInfo() {
        System.out.println("\n=== THÔNG TIN TÀI KHOẢN BIDDER ===");
        System.out.println("Username: " + username);
        System.out.println("Email: " + email);
        System.out.println("Số dư ví: $" + balance);
    }
}