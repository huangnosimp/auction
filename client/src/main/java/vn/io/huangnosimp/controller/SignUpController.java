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
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField passwordTextField;
    @FXML private TextField confirmPasswordTextField;
    @FXML private CheckBox showPasswordCheck;
    @FXML private CheckBox termsCheckBox;

    @FXML
    public void handleFullNameEnter(ActionEvent event) {
        userNameField.requestFocus();
    }

    @FXML
    public void handleUserNameEnter(ActionEvent event) {
        emailField.requestFocus();
    }

    @FXML
    public void handleEmailEnter(ActionEvent event) {
        if (showPasswordCheck != null && showPasswordCheck.isSelected()) {
            passwordTextField.requestFocus();
        } else {
            passwordField.requestFocus();
        }
    }

    @FXML
    public void handlePasswordEnter(ActionEvent event) {
        if (showPasswordCheck != null && showPasswordCheck.isSelected()) {
            confirmPasswordTextField.requestFocus();
        } else {
            confirmPasswordField.requestFocus();
        }
    }

    @FXML
    public void handleConfirmPasswordEnter(ActionEvent event) {
        termsCheckBox.requestFocus();
    }

    @FXML
    private void togglePasswordVisibility() {
        if (showPasswordCheck.isSelected()) {
            passwordTextField.setText(passwordField.getText());
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);

            confirmPasswordTextField.setText(confirmPasswordField.getText());
            confirmPasswordTextField.setVisible(true);
            confirmPasswordTextField.setManaged(true);
            confirmPasswordField.setVisible(false);
            confirmPasswordField.setManaged(false);
            
            passwordTextField.requestFocus();
            passwordTextField.positionCaret(passwordTextField.getText().length());
        } else {
            passwordField.setText(passwordTextField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);

            confirmPasswordField.setText(confirmPasswordTextField.getText());
            confirmPasswordField.setVisible(true);
            confirmPasswordField.setManaged(true);
            confirmPasswordTextField.setVisible(false);
            confirmPasswordTextField.setManaged(false);
            
            passwordField.requestFocus();
            passwordField.positionCaret(passwordField.getText().length());
        }
    }

    @FXML
    private void handleSignUp(ActionEvent event) {
        String fullName = fullNameField.getText().trim();
        String username = userNameField.getText().trim();
        String email = emailField.getText().trim();
        String pass = (showPasswordCheck != null && showPasswordCheck.isSelected()) ? passwordTextField.getText() : passwordField.getText();
        String confirmPass = (showPasswordCheck != null && showPasswordCheck.isSelected()) ? confirmPasswordTextField.getText() : confirmPasswordField.getText();

        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", "Please fill in all fields!");
            return;
        }

        // Logic kiểm tra cơ bản

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