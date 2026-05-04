package vn.io.huangnosimp.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import vn.io.huangnosimp.Manager.SocketManager;
import vn.io.huangnosimp.Manager.ViewManager;
import vn.io.huangnosimp.dto.request.RegisterRequestDTO;
import vn.io.huangnosimp.enums.UserType;
import vn.io.huangnosimp.protocol.ActionType;
import vn.io.huangnosimp.protocol.Request;
import vn.io.huangnosimp.protocol.ResponseStatus;

public class SignUpController {

    @FXML private TextField fullNameField;
    @FXML private TextField userNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private CheckBox termsCheckBox;

    @FXML
    private void handleSignUp(ActionEvent event) {
        String fullName = fullNameField.getText();
        String username = userNameField.getText();
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
        RegisterRequestDTO registerRequestDTO = new RegisterRequestDTO(UserType.MEMBER, username, pass, email);
        Request registerRequest = new Request(ActionType.REGISTER, registerRequestDTO);
        SocketManager.getClient().sendRequestAsync(registerRequest)
                .thenAccept(registerResponse -> {
                    if(ResponseStatus.SUCCESS.equals(registerResponse.getStatus())){
                        Platform.runLater(()-> {
                                    showAlert(Alert.AlertType.INFORMATION, "Success", "Account created successfully! Please login.");
                                    ViewManager.changeMainStage("LoginView.fxml");
                                }
                        );
                    }
                    else if (ResponseStatus.CONFLICT.equals(registerResponse.getStatus())) {

                    }
                    else if (ResponseStatus.FAILED.equals(registerResponse.getStatus())) {

                    }
                });
    }

    @FXML
    private void navigateToLogin() {
        System.out.println("Navigating back to Login...");
        ViewManager.changeMainStage("login.fxml");
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