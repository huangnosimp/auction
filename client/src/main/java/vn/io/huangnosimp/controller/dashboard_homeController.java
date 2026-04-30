package vn.io.huangnosimp.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
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
    @FXML private FlowPane auctionFlowPane;
    @FXML private ScrollPane scrollJoined;
    @FXML private ScrollPane scrollMyAuctions;
    @FXML private Button tabJoined;
    @FXML private Button tabMyAuctions;

    public void addToDashboard(AuctionCardDTO dto, String type){
        String fxmlPath = null;
        if(type.equals("Joining")){
            fxmlPath = "/fxml/JoiningauctionCard.fxml";
        }
        else if (type.equals("My")) {
            fxmlPath = "/fxml/MyauctionCard.fxml";
        }
        else{
            fxmlPath = "/fxml/PublicauctionCard.fxml";
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent cardNode = loader.load();
            ItemCardController cardController = loader.getController();
            cardController.addInfo(dto, type);
            cardNode.setUserData(dto.getAuctionId());
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
    public void removeFromDashBoard(String auctionId) {
        Node nodeToRemove = auctionFlowPane.getChildren().stream()
                .filter(node -> auctionId.equals(node.getUserData()))
                .findFirst()
                .orElse(null);


        if (nodeToRemove != null) {
            FadeTransition ft = new FadeTransition(Duration.millis(500), nodeToRemove);
            ft.setFromValue(1.0);
            ft.setToValue(0.0);

            ft.setOnFinished(event -> {
                auctionFlowPane.getChildren().remove(nodeToRemove);
            });

            ft.play();
        }
    }
    @FXML
    private void handleTabChange(ActionEvent event) {
        tabJoined.getStyleClass().remove("tab-btn-active");
        tabMyAuctions.getStyleClass().remove("tab-btn-active");

        if (event.getSource() == tabJoined) {
            tabJoined.getStyleClass().add("tab-btn-active");

            scrollJoined.setVisible(true);
            scrollJoined.setManaged(true);
            scrollMyAuctions.setVisible(false);
            scrollMyAuctions.setManaged(false);
        } else {
            tabMyAuctions.getStyleClass().add("tab-btn-active");

            scrollJoined.setVisible(false);
            scrollJoined.setManaged(false);
            scrollMyAuctions.setVisible(true);
            scrollMyAuctions.setManaged(true);
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