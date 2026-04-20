package client.info;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("ĐĂNG KÝ TÀI KHOẢN ĐẤU GIÁ");

        System.out.print("Nhập tên đăng nhập (Username): ");
        String user = sc.nextLine();

        System.out.print("Nhập họ và tên: ");
        String name = sc.nextLine();

        System.out.print("Nhập địa chỉ Email: ");
        String email = sc.nextLine();

        System.out.print("Nhập số tiền nạp vào ví: ");
        double money = 0;
        try {
            money = Double.parseDouble(sc.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Lỗi: Số tiền không hợp lệ, mặc định đặt là 0.");
        }

        Client newUser = new Client(user, name, email, money);
        newUser.showInfo();
        System.out.println("Chúc mừng " + user + " đã đăng ký thành công!");
        sc.close();
    }
}