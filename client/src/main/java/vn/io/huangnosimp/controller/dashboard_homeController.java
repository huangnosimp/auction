package vn.io.huangnosimp.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.event.ActionEvent; // đúng loại sự kiện
import vn.io.huangnosimp.Manager.ControllerManager;
import vn.io.huangnosimp.Manager.ViewManager;
import vn.io.huangnosimp.dto.response.AuctionCardDTO;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class dashboard_homeController implements Initializable {
    @FXML private Button btnFab1;
    @FXML private FlowPane auctionFlowPane;

    public void addToDashboard(AuctionCardDTO dto){
        try {
            String fxmlPath = "/fxml/ItemCard.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent cardNode = loader.load();
            ItemCardController cardController = loader.getController();
            cardController.addInfo(dto, "Inspect");
            auctionFlowPane.getChildren().add(cardNode);
            FadeTransition ft = new FadeTransition(Duration.millis(500), cardNode);
            ft.setFromValue(0.0);
            ft.setToValue(1.0);
            ft.play();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
    public void handleCreateAuction(ActionEvent event){
        ViewManager.changeView("create_auction.fxml", 2);

    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ControllerManager.setDashboardHomeController(this);
    }
}