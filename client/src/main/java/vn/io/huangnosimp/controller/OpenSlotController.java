package vn.io.huangnosimp.controller;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.FlowPane;
import javafx.util.Duration;
import vn.io.huangnosimp.Manager.ControllerManager;
import vn.io.huangnosimp.dto.response.AuctionCardDTO;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class OpenSlotController implements Initializable {
    @FXML private FlowPane cardContainer;
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
    public void initialize(URL location, ResourceBundle resources){
        ControllerManager.setOpenSlotController(this);
    }
}
