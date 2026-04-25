package vn.io.huangnosimp.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class SignUpController {

    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private CheckBox termsCheckBox;

    @FXML
    private void handleSignUp(ActionEvent event) {
        String fullName = fullNameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String pass = passwordField.getText();
        String confirmPass = confirmPasswordField.getText();

        // Logic kiểm tra cơ bản
        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || pass.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", "Please fill in all fields!");
            return;
        }

        if (!pass.equals(confirmPass)) {
            showAlert(Alert.AlertType.ERROR, "Password Mismatch", "Passwords do not match!");
            return;
        }

        if (!termsCheckBox.isSelected()) {
            showAlert(Alert.AlertType.WARNING, "Terms & Conditions", "You must agree to the terms to continue.");
            return;
        }

        // Nếu mọi thứ ổn, thực hiện đăng ký
        System.out.println("Registering user: " + email);
        showAlert(Alert.AlertType.INFORMATION, "Success", "Account created successfully! Please login.");
    }

    @FXML
    private void navigateToLogin() {
        // Code chuyển scene về Login.fxml tại đây
        System.out.println("Navigating back to Login...");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        // Style cho Alert cũng có thể tùy chỉnh CSS nếu cần
        alert.showAndWait();
    }
}