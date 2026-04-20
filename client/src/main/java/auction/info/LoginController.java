package auction.info;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisible;
    @FXML private CheckBox rememberMe;
    @FXML private Label errorLabel;
    @FXML private Button loginBtn;
    @FXML private Button togglePasswordBtn;

    private boolean isPasswordVisible = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Sync text giữa PasswordField và TextField khi toggle
        passwordVisible.textProperty().bindBidirectional(passwordField.textProperty());

        // Hover effect cho nút đăng nhập
        loginBtn.setOnMouseEntered(e ->
                loginBtn.setStyle(loginBtn.getStyle().replace("#8B1A1A", "#6e1515")));
        loginBtn.setOnMouseExited(e ->
                loginBtn.setStyle(loginBtn.getStyle().replace("#6e1515", "#8B1A1A")));
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        errorLabel.setVisible(false);

        if (username.isEmpty() || password.isEmpty()) {
            showError("Vui lòng nhập đầy đủ thông tin đăng nhập.");
            return;
        }

        // TODO: Gọi service xác thực
        // authService.login(username, password);

        System.out.println("Đăng nhập với: " + username);
    }

    @FXML
    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            passwordVisible.setVisible(true);
            passwordVisible.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            togglePasswordBtn.setText("🙈");
        } else {
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordVisible.setVisible(false);
            passwordVisible.setManaged(false);
            togglePasswordBtn.setText("👁");
        }
    }

    @FXML
    private void handleForgotPassword() {
        // TODO: Mở màn hình quên mật khẩu
        System.out.println("Quên mật khẩu được nhấn");
    }

    @FXML
    private void handleRegister() {
        // TODO: Mở màn hình đăng ký
        System.out.println("Đăng ký ngay được nhấn");
    }

    @FXML
    private void handleVNeID() {
        // TODO: Tích hợp đăng nhập VNeID
        System.out.println("Đăng nhập VNeID được nhấn");
    }

    @FXML
    private void handleClose() {
        // TODO: Đóng dialog hoặc quay về màn hình trước
        usernameField.getScene().getWindow().hide();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}