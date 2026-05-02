package vn.io.huangnosimp.controller;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
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
import vn.io.huangnosimp.Manager.ViewManager;
import vn.io.huangnosimp.dto.request.GetPublicAcutionCardDTO;
import vn.io.huangnosimp.dto.response.AuctionCardDTO;
import vn.io.huangnosimp.protocol.ActionType;
import vn.io.huangnosimp.protocol.Request;

import java.net.URL;
import java.util.ResourceBundle;

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
    public void handlebtnOpenSlots(ActionEvent event){
        GetPublicAcutionCardDTO getPublicAcutionCard = new GetPublicAcutionCardDTO(30);
        Request request = new Request(ActionType.GET_PUBLIC_AUCTION_CARD, getPublicAcutionCard);
        SocketManager.getClient().sendRequestAsync(request)
                        .thenAccept(response -> {

                        });




        changeView("open_slots.fxml", 1);
        handleMenuAction(event);
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
        lblBalance.setText(String.valueOf(getDashboardInfo().getBalance()));
        ControllerManager.getDashboardHomeController().setupDashboardHome();
    }
    @Override
    public void initialize(URL location, ResourceBundle resources){
        setMainBorderPane(mainBorderPane);
        ControllerManager.setDashboardController(this);
        setupDashboard();
        changeView("dashboard_home.fxml", 1);
    }
}
