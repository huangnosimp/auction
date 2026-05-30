package vn.io.huangnosimp.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import vn.io.huangnosimp.Manager.SceneManager;
import vn.io.huangnosimp.Manager.SocketManager;
import vn.io.huangnosimp.Manager.TimeSyncManager;
import vn.io.huangnosimp.Manager.UserSession;
import vn.io.huangnosimp.Manager.ViewManager;
import vn.io.huangnosimp.dto.request.GetPostedAuctionDTO;
import vn.io.huangnosimp.dto.request.LoginRequestDTO;
import vn.io.huangnosimp.dto.response.DashboardResponseDTO;
import vn.io.huangnosimp.dto.response.AutoBidResponseDTO;
import vn.io.huangnosimp.dto.response.GetPostedAuctionCardResponseDTO;
import vn.io.huangnosimp.enums.UserType;
import vn.io.huangnosimp.protocol.ActionType;
import vn.io.huangnosimp.protocol.Request;
import vn.io.huangnosimp.protocol.Response;
import vn.io.huangnosimp.protocol.ResponseStatus;
import vn.io.huangnosimp.util.GsonParser;
import vn.io.huangnosimp.dto.response.AuctionCardDTO;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import java.util.ArrayList;

import static vn.io.huangnosimp.Manager.ViewManager.changeMainStage;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ToggleButton userToggle;
    @FXML private ToggleButton adminToggle;
    @FXML private ToggleGroup roleGroup;
    @FXML private Button loginButton;
    private UserType userType;

    @FXML
    public void handleEmailEnter(ActionEvent event) {
        passwordField.requestFocus();
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

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
                            TimeSyncManager.syncAsync(SocketManager.getClient()).exceptionally(ex -> null);
                            Request activeAutobidRequest = new Request(ActionType.GET_USER_AUTO_BIDS, null);
                            Request getMineRequest = new Request(ActionType.GET_POSTED_AUCTION_CARD, new GetPostedAuctionDTO(30));
                            Request dashboardRequest = new Request(ActionType.GET_DASHBOARD_INFO, null);

                            SocketManager.getClient().sendRequestAsync(getMineRequest)
                                    .thenCombine(SocketManager.getClient().sendRequestAsync(dashboardRequest), (getPostedResponse, dashboardResponse) -> {
                                        return new Response[]{getPostedResponse, dashboardResponse};
                                    })
                                    .thenCombine(SocketManager.getClient().sendRequestAsync(activeAutobidRequest), (responses, autoBidResponse) -> {
                                        Response getPostedResponse = responses[0];
                                        Response dashboardResponse = responses[1];

                                        GetPostedAuctionCardResponseDTO postedDto = GsonParser.GSON.fromJson(GsonParser.GSON.toJsonTree(getPostedResponse.getData()), GetPostedAuctionCardResponseDTO.class);
                                        UserSession.addMyCard(postedDto.getAuctionCards());

                                        DashboardResponseDTO dashboardDto = GsonParser.GSON.fromJson(GsonParser.GSON.toJsonTree(dashboardResponse.getData()), DashboardResponseDTO.class);

                                        Type autoBidListType = new TypeToken<List<AutoBidResponseDTO>>(){}.getType();
                                        List<AutoBidResponseDTO> autoBids = GsonParser.GSON.fromJson(
                                                GsonParser.GSON.toJsonTree(autoBidResponse.getData()), autoBidListType);

                                        Platform.runLater(() -> {
                                            UserSession.setDashboardInfo(dashboardDto);
                                            UserSession.setUsername(dashboardDto.getUsername());
                                            UserSession.setEmail(dashboardDto.getEmail());
                                            UserSession.setBalance(dashboardDto.getBalance());
                                            UserSession.setActiveAutoBids(autoBids);

                                            List<AuctionCardDTO> joinedRooms = new ArrayList<>();
                                            if (dashboardDto.getAuctionCardInfo() != null) {
                                                for (AuctionCardDTO card : dashboardDto.getAuctionCardInfo()) {
                                                    boolean isManaged = UserSession.getMyListCard().stream()
                                                            .anyMatch(myCard -> myCard.getAuctionId().equals(card.getAuctionId()));
                                                    if (!isManaged) {
                                                        joinedRooms.add(card);
                                                    }
                                                }
                                            }
                                            UserSession.addJoiningCard(joinedRooms);

                                            changeMainStage("dashboard.fxml");
                                            SceneManager.getStage().setMaximized(true);
                                        });
                                        return null;
                                    });
                        }
                        else if (ResponseStatus.UNAUTHORIZED.equals(response.getStatus())){
                            Platform.runLater(()-> showAlert("UNAUTHORIZED", response.getMessage()));
                        }
                        else if (ResponseStatus.FAILED.equals(response.getStatus())){
                            Platform.runLater(()-> showAlert("FAILED", response.getMessage()));
                        }
                    });
        }
        else {
            LoginRequestDTO loginRequestDTO = new LoginRequestDTO(UserType.ADMIN, email, password);
            Request request = new Request(ActionType.LOGIN, loginRequestDTO);
            SocketManager.getClient().sendRequestAsync(request)
                    .thenAccept(response -> {
                        if (ResponseStatus.SUCCESS.equals(response.getStatus())) {
                            TimeSyncManager.syncAsync(SocketManager.getClient()).exceptionally(ex -> null);
                            Platform.runLater(()-> changeMainStage("AdminDashboard.fxml"));
                        }
                        else if (ResponseStatus.UNAUTHORIZED.equals(response.getStatus())){
                            Platform.runLater(()-> showAlert("UNAUTHORIZED", response.getMessage()));
                        }
                        else if (ResponseStatus.FAILED.equals(response.getStatus())){
                            Platform.runLater(()-> showAlert("FAILED", response.getMessage()));
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
