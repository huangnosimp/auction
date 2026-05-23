package vn.io.huangnosimp.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import vn.io.huangnosimp.dto.response.AuctionCardDTO;

import java.io.IOException;
import java.util.List;

public class InventoryController {
    @FXML private VBox listingsContainer;
    @FXML private VBox wonContainer;
    public void addMyItem(List<AuctionCardDTO> myList){
        listingsContainer.getChildren().clear();
        for(AuctionCardDTO dto : myList){
            Platform.runLater(()->{
                try{
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Listingrowitem.fxml"));
                    Parent node = loader.load();
                    RowItemController controller = loader.getController();
                    controller.setUpListingRow(dto);
                    listingsContainer.getChildren().add(node);
                }
                catch (IOException e){
                    e.printStackTrace();
                }
            });
        }
    }
}
