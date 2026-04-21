package vn.io.huangnosimp.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;

import javafx.event.ActionEvent;
import java.io.IOException;

public class ItemCardController {
    @FXML
    public void handleBidClick(ActionEvent event){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/liveAuction.fxml"));
            Parent chartView = loader.load();
            Button bidButton = (Button) event.getSource();
            BorderPane mainRoot = (BorderPane) bidButton.getScene().lookup("#mainBorderPane");
            if(mainRoot != null){
                mainRoot.setCenter(chartView);
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
