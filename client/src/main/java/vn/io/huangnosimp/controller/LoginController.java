package vn.io.huangnosimp.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import vn.io.huangnosimp.Manager.SocketManager;
import vn.io.huangnosimp.Manager.UserSession;
import vn.io.huangnosimp.Manager.ViewManager;
import vn.io.huangnosimp.dto.request.GetAuctionDetailRequestDTO;
import vn.io.huangnosimp.dto.request.LoginRequestDTO;
import vn.io.huangnosimp.dto.response.DashboardResponseDTO;
import vn.io.huangnosimp.enums.UserType;
import vn.io.huangnosimp.network.SocketClient;
import vn.io.huangnosimp.protocol.ActionType;
import vn.io.huangnosimp.protocol.Request;
import vn.io.huangnosimp.protocol.ResponseStatus;
import vn.io.huangnosimp.util.GsonParser;

import static vn.io.huangnosimp.Manager.ViewManager.changeMainStage;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ToggleButton userToggle;
    @FXML private ToggleButton adminToggle;
    @FXML private ToggleGroup roleGroup;
    private UserType userType;

    @FXML
    public void pressEnter(){

    }
    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        // Kiểm tra xem là Admin hay Customer đang chọn
        boolean isAdmin = adminToggle.isSelected();
        if(isAdmin){
            userType = UserType.ADMIN;
        }
        else {
            userType = UserType.MEMBER;
        }

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter all credentials!");
            return;
        }
        if(userType.equals(UserType.MEMBER)) {
            LoginRequestDTO loginRequestDTO = new LoginRequestDTO(UserType.MEMBER, email, password);
            Request request = new Request(ActionType.LOGIN, loginRequestDTO);
            SocketManager.getClient().sendRequestAsync(request)
                    .thenAccept(response -> {

                        if (ResponseStatus.SUCCESS.equals(response.getStatus())) {

                            Request dashboardRequest = new Request(ActionType.GET_DASHBOARD_INFO, null);
                            SocketManager.getClient().sendRequestAsync(dashboardRequest)
                                    .thenAccept(dashboardResponse->{
                                        DashboardResponseDTO dto = GsonParser.GSON.fromJson(GsonParser.GSON.toJsonTree(dashboardResponse.getData()), DashboardResponseDTO.class);
                                        Platform.runLater(() -> {
                                            UserSession.setDashboardInfo(dto);
                                            changeMainStage("dashboard.fxml");
                                        });
                                    });
                        }

                        else if (ResponseStatus.UNAUTHORIZED.equals(response.getStatus())){
                            showAlert("UNAUTHORIZED", response.getMessage());
                        }

                        else if (ResponseStatus.FAILED.equals(response.getStatus())){
                            showAlert("FAILED", response.getMessage());
                        }
                    });
        }


    }

    @FXML
    private void handleRegister() {
        ViewManager.changeMainStage("SignUp.fxml");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}