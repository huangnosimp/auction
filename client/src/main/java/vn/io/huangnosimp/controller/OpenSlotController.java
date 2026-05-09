package vn.io.huangnosimp.controller;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.FlowPane;
import javafx.util.Duration;
import vn.io.huangnosimp.Manager.ControllerManager;
import vn.io.huangnosimp.Manager.UserSession;
import vn.io.huangnosimp.dto.response.AuctionCardDTO;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class OpenSlotController implements Initializable {
    @FXML private FlowPane cardContainer;

    public FlowPane getCardContainer(){
        return cardContainer;
    }

    public void clearContainer(){
        getCardContainer().getChildren().clear();
    }

    public void addCard(AuctionCardDTO dto, String type){
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

        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent cardNode = loader.load();
            ItemCardController cardController = loader.getController();
            cardController.addInfo(dto, type);
            cardNode.setUserData(dto.getAuctionId());
            cardContainer.getChildren().add(cardNode);
            FadeTransition ft = new FadeTransition(Duration.millis(500), cardNode);
            ft.setFromValue(0.0);
            ft.setToValue(1.0);
            ft.play();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
    public void clearCard(String auctionId){
        Node nodeToRemove = cardContainer.getChildren().stream()
                .filter(node -> auctionId.equals(node.getUserData()))
                .findFirst()
                .orElse(null);


        if (nodeToRemove != null) {
            FadeTransition ft = new FadeTransition(Duration.millis(500), nodeToRemove);
            ft.setFromValue(1.0);
            ft.setToValue(0.0);

            ft.setOnFinished(event -> {
                cardContainer.getChildren().remove(nodeToRemove);
            });

            ft.play();
        }
    }
    public void setupOpenSlot(List<AuctionCardDTO> listcard){
        for(AuctionCardDTO dto : listcard){
            addCard(dto, "Public");
        }
    }
    public void initialize(URL location, ResourceBundle resources){
        ControllerManager.setOpenSlotController(this);
        setupOpenSlot(UserSession.getListCard());
    }
}
