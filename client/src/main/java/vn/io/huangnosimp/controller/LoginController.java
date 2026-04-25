package vn.io.huangnosimp.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ToggleButton userToggle;
    @FXML private ToggleButton adminToggle;
    @FXML private ToggleGroup roleGroup;

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        // Kiểm tra xem là Admin hay Customer đang chọn
        boolean isAdmin = adminToggle.isSelected();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter all credentials!");
            return;
        }

        if (isAdmin) {
            System.out.println("Logging in as ADMIN: " + email);
        } else {
            System.out.println("Logging in as CUSTOMER: " + email);
        }
    }

    @FXML
    private void handleRegister() {
        System.out.println("Navigate to Register Screen");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}