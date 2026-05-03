package vn.io.huangnosimp.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.event.ActionEvent; // đúng loại sự kiện
import vn.io.huangnosimp.Manager.ControllerManager;
import vn.io.huangnosimp.Manager.SocketManager;
import vn.io.huangnosimp.Manager.ViewManager;
import vn.io.huangnosimp.dto.response.AuctionCardDTO;
import vn.io.huangnosimp.dto.response.DashboardResponseDTO;
import vn.io.huangnosimp.network.IServerMessageListener;
import vn.io.huangnosimp.protocol.ActionType;
import vn.io.huangnosimp.protocol.Request;
import vn.io.huangnosimp.protocol.Response;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static vn.io.huangnosimp.Manager.UserSession.getDashboardInfo;

public class dashboard_homeController implements Initializable, IServerMessageListener {
    @FXML private FlowPane auctionFlowPane;
    @FXML private FlowPane flowJoined;
    @FXML private ScrollPane scrollJoined;
    @FXML private ScrollPane scrollMyAuctions;
    @FXML private Button tabJoined;
    @FXML private Button tabMyAuctions;
    @FXML private Label statActiveBids;
    @FXML private Label statWinning;
    @FXML private Label statOutbid;
    @FXML private Label statWonTotal;

    public FlowPane getAuctionFlowPane(){return auctionFlowPane;}
    public FlowPane getFlowJoined(){return flowJoined;}
    public void setupDashboardHome(){
        statActiveBids.setText(String.valueOf(getDashboardInfo().getJoinedRooms()));
        statWinning.setText(String.valueOf(getDashboardInfo().getWinningBids()));
        statOutbid.setText(String.valueOf(getDashboardInfo().getOutBids()));
        statWonTotal.setText(String.valueOf(getDashboardInfo().getWonTotal()));
        for(AuctionCardDTO DTO : getDashboardInfo().getAuctionCardInfo()){
            addToDashboard(DTO, "Joining", flowJoined);
        }
    }
    public void addToDashboard(AuctionCardDTO dto, String type, FlowPane auctionFlowPane){
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
    public void removeFromDashBoard(String auctionId, FlowPane auctionFlowPane) {
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
    public void changeTab(){
        tabJoined.getStyleClass().remove("tab-btn-active");
        tabMyAuctions.getStyleClass().remove("tab-btn-active");
        tabMyAuctions.getStyleClass().add("tab-btn-active");

        scrollJoined.setVisible(false);
        scrollJoined.setManaged(false);
        scrollMyAuctions.setVisible(true);
        scrollMyAuctions.setManaged(true);
    }
    public void handleCreateAuction(ActionEvent event){
        ViewManager.changeView("create_auction.fxml", 2);

    }

    public void onResponseReceived(Response response){

    }
    public void onResponseReceived(Response response, ActionType actionType){}
    public void onRequestReceived(Request request){}
    public void onDisconnected(String reason){}
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ControllerManager.setDashboardHomeController(this);
        setupDashboardHome();
    }
}