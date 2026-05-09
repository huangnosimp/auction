package vn.io.huangnosimp.controller;

import com.google.gson.reflect.TypeToken;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import javafx.event.ActionEvent;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import vn.io.huangnosimp.Manager.ControllerManager;
import vn.io.huangnosimp.Manager.SocketManager;
import vn.io.huangnosimp.Manager.UserSession;
import vn.io.huangnosimp.Manager.ViewManager;
import vn.io.huangnosimp.dto.request.GetPublicAcutionCardDTO;
import vn.io.huangnosimp.dto.response.AuctionCardDTO;
import vn.io.huangnosimp.dto.response.AuctionDetailResponseDTO;
import vn.io.huangnosimp.dto.response.DashboardResponseDTO;
import vn.io.huangnosimp.dto.response.GetPublicAuctionCardResponseDTO;
import vn.io.huangnosimp.protocol.ActionType;
import vn.io.huangnosimp.protocol.Request;
import vn.io.huangnosimp.protocol.ResponseStatus;
import vn.io.huangnosimp.util.GsonParser;

import javax.swing.text.View;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.*;

import static vn.io.huangnosimp.Manager.FormatUtil.formatNumber;
import static vn.io.huangnosimp.Manager.UserSession.getDashboardInfo;
import static vn.io.huangnosimp.Manager.ViewManager.*;


public class DashboardController implements Initializable {
    @FXML private BorderPane mainBorderPane;
    @FXML private StackPane contentArea; // Cái này nằm ở file dashboard.fxml nên giữ lại
    @FXML private Button btnOpenSlots;
    @FXML private HBox menuHbox;
    @FXML private VBox sideVbox;
    @FXML private Label lblBalance;

    @FXML private HBox toastBox;
    @FXML private Label toastTitle, toastSub, toastIconLabel;
    @FXML private Circle toastIconCircle;

    public static DashboardController instance;

    @FXML
    public void handleMenuAction(ActionEvent event){
        Button clickedButton = (Button) event.getSource();

        for(Node node : menuHbox.getChildren()){
            if(node instanceof Button){
                Button btn = (Button) node;
                btn.getStyleClass().remove("nav-btn-active");
            }
        }
        clickedButton.getStyleClass().add("nav-btn-active");
    }

    public Button getActiveMenuButton(){
        for(Node node : menuHbox.getChildren()){
            if(node instanceof Button){
                Button actButton = (Button) node;
                if (actButton.getStyleClass().contains("nav-btn-active")){
                    return actButton;
                }
            }
        }
        return null;
    }

    @FXML
    public void handlebtnAvatar(ActionEvent event){
        changeView("AccountView.fxml", 1);
    }

    @FXML
    public void handlebtnDashboard(ActionEvent event){
        changeView("dashboard_home.fxml", 1);
        handleMenuAction(event);
    }

    @FXML
    public void handlebtnOpenSlots(ActionEvent event) {
        List<AuctionCardDTO> combineList = new ArrayList<>(UserSession.getJoiningListCard());
        combineList.addAll(UserSession.getMyListCard());
        List<AuctionCardDTO> result = new ArrayList<>();
        fetchUntilFull(result, combineList, 30, event, 5);
        if(ViewManager.getCache().containsKey("open_slots.fxml")){
            ControllerManager.getOpenSlotController().clearContainer();
            ControllerManager.getOpenSlotController().setupOpenSlot(UserSession.getListCard());
        }
    }

    private void fetchUntilFull(List<AuctionCardDTO> result,
                                List<AuctionCardDTO> excludeList,
                                int target,
                                ActionEvent event,
                                int maxRetry) {

        // Đủ số lượng hoặc hết retry → chạy UI
        if (result.size() >= target || maxRetry <= 0) {
            Platform.runLater(() -> {
                UserSession.addToList(result);
                ViewManager.changeView("open_slots.fxml", 1);
                handleMenuAction(event);
            });
            return;
        }

        int needed = target - result.size();

        // Lấy gấp đôi để bù cho card bị lọc
        GetPublicAcutionCardDTO requestDTO = new GetPublicAcutionCardDTO(needed * 2);
        Request request = new Request(ActionType.GET_PUBLIC_AUCTION_CARD, requestDTO);

        SocketManager.getClient().sendRequestAsync(request)
                .thenAccept(response -> {
                    if (!ResponseStatus.SUCCESS.equals(response.getStatus())) {
                        // Request thất bại → dừng, dùng những gì đã có
                        Platform.runLater(() -> {
                            UserSession.addToList(result);
                            ViewManager.changeView("open_slots.fxml", 1);
                            handleMenuAction(event);
                        });
                        return;
                    }

                    GetPublicAuctionCardResponseDTO dto = GsonParser.GSON.fromJson(
                            GsonParser.GSON.toJsonTree(response.getData()),
                            GetPublicAuctionCardResponseDTO.class
                    );

                    List<AuctionCardDTO> responseList = dto.getPublicAuctionCardList();

                    // Server không còn card → dừng
                    if (responseList == null || responseList.isEmpty()) {
                        Platform.runLater(() -> {
                            UserSession.addToList(result);
                            ViewManager.changeView("open_slots.fxml", 1);
                            handleMenuAction(event);
                        });
                        return;
                    }

                    // Tập hợp id cần loại trừ (excludeList + result đã có)
                    Set<String> excludeIds = new HashSet<>();
                    excludeList.forEach(c -> excludeIds.add(c.getAuctionId()));
                    result.forEach(c -> excludeIds.add(c.getAuctionId()));

                    // Lọc và thêm vào result
                    for (AuctionCardDTO card : responseList) {
                        if (!excludeIds.contains(card.getAuctionId())) {
                            result.add(card);
                            excludeIds.add(card.getAuctionId());
                            if (result.size() >= target) break;
                        }
                    }

                    // Đủ rồi → chạy UI, chưa đủ → gọi lại với retry - 1
                    if (result.size() >= target) {
                        Platform.runLater(() -> {
                            UserSession.addToList(result);
                            ViewManager.changeView("open_slots.fxml", 1);
                            handleMenuAction(event);
                        });
                    } else {
                        fetchUntilFull(result, excludeList, target, event, maxRetry - 1);
                    }
                });
    }
    public void showToast(String title, String sub, boolean success) {
        String borderColor = success ? "#22c55e" : "#e24b4a";
        String titleColor  = success ? "#4ade80" : "#f87171";
        String subColor    = success ? "#86efac" : "#fca5a5";
        String bgColor     = success ? "#1a2e1a" : "#2e1a1a";

        toastTitle.setText(title);
        toastSub.setText(sub);
        toastTitle.setStyle("-fx-text-fill: " + titleColor + "; -fx-font-size: 12px; -fx-font-weight: bold;");
        toastSub.setStyle("-fx-text-fill: " + subColor + "; -fx-font-size: 10px;");
        toastIconCircle.setFill(javafx.scene.paint.Color.web(borderColor));
        toastIconLabel.setText(success ? "✓" : "✕");
        toastBox.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-border-color: " + borderColor + ";" +
                        "-fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 10, 0, 0, 4);"
        );

        toastBox.setVisible(true);
        toastBox.setManaged(true);

        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> {
            FadeTransition fade = new FadeTransition(Duration.millis(400), toastBox);
            fade.setToValue(0);
            fade.setOnFinished(f -> {
                toastBox.setVisible(false);
                toastBox.setManaged(false);
                toastBox.setOpacity(1);
            });
            fade.play();
        });
        pause.play();
    }
    private void setupDashboard(){
        lblBalance.setText(calculateBalance(getDashboardInfo().getBalance()));
        ControllerManager.getDashboardHomeController().setupDashboardHome();
    }
    private String calculateBalance(double value){
        if(value >= 1000000000){
            return formatNumber(value/1000000000)+" B";
        }
        else if (value>=1000000){
            return formatNumber(value/1000000)+" M";
        }
        else {
            return formatNumber(value/1000)+" K";
        }
    }
    @Override
    public void initialize(URL location, ResourceBundle resources){
        setMainBorderPane(mainBorderPane);
        ControllerManager.setDashboardController(this);
        setupDashboard();
        changeView("dashboard_home.fxml", 1);
    }
}
