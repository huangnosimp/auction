package vn.io.huangnosimp.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import vn.io.huangnosimp.Manager.SocketManager;
import vn.io.huangnosimp.Manager.UserSession;
import vn.io.huangnosimp.dto.request.LoginRequestDTO;
import vn.io.huangnosimp.dto.response.DashboardResponseDTO;
import vn.io.huangnosimp.enums.UserType;
import vn.io.huangnosimp.network.SocketClient;
import vn.io.huangnosimp.protocol.ActionType;
import vn.io.huangnosimp.protocol.Request;
import vn.io.huangnosimp.protocol.ResponseStatus;

import java.net.URL;
import java.util.ResourceBundle;

import static vn.io.huangnosimp.Manager.ViewManager.changeMainStage;

public class LoginController implements Initializable {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisible;
    @FXML private CheckBox rememberMe;
    @FXML private Label errorLabel;
    @FXML private Button loginBtn;
    @FXML private Button togglePasswordBtn;

    private boolean isPasswordVisible = false;
    private SocketClient socketClient = SocketManager.getClient();

    @FXML
    private Parent rootNode;

    private UserType getCurrentScreenType() {
        String id = rootNode.getId();
        if ("ADMIN_LOGIN_VIEW".equals(id)) {
            return UserType.ADMIN;
        }
        return UserType.MEMBER;
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
        if(getCurrentScreenType().equals(UserType.MEMBER)) {
            LoginRequestDTO loginRequestDTO = new LoginRequestDTO(UserType.MEMBER, username, password);
            Request request = new Request(ActionType.LOGIN, loginRequestDTO);
            socketClient.sendRequestAsync(request)
                    .thenAccept(response -> {
                        // 1. Xử lý logic dữ liệu ở luồng nền (không cần Platform.runLater)
                        if (ResponseStatus.SUCCESS.equals(response.getStatus())) {

                            // Dùng hàm convert để tránh lỗi ép kiểu
                            DashboardResponseDTO dashboardResponse = response.getDataAs(DashboardResponseDTO.class);
                            UserSession.setDashboardInfo(dashboardResponse);

                            // 2. Chỉ dùng Platform.runLater khi chuyển màn hình (Tác động UI)
                            Platform.runLater(() -> {
                                NavigationManager.switchScene("/views/dashboard.fxml");
                            });

                        } else {
                            // Hiển thị lỗi nếu đăng nhập thất bại
                            Platform.runLater(() -> {
                                showErrorAlert(response.getMessage());
                            });
                        }
                    });
        }
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
         changeMainStage("ForgotPassword.fxml");
    }

    @FXML
    private void handleRegister() {
        changeMainStage("Register.fxml");
    }

    @FXML
    private void handleVNeID() {
        // TODO: Tích hợp đăng nhập VNeID
        System.out.println("Đăng nhập VNeID được nhấn");
    }

    @FXML
    private void handleClose() {
        changeMainStage("LoginView.fxml");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}