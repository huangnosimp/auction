package vn.io.huangnosimp.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import vn.io.huangnosimp.model.AuctionHistory;


public class MyAuctionsController {
    @FXML private TableView<AuctionHistory> auctionTable;
    @FXML private TableColumn<AuctionHistory, String> colId;
    @FXML private TableColumn<AuctionHistory, String> colItem;
    @FXML private TableColumn<AuctionHistory, String> colMyBid;
    @FXML private TableColumn<AuctionHistory, String> colStatus;
    @FXML private TableColumn<AuctionHistory, String> colTime;

    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colItem.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colMyBid.setCellValueFactory(new PropertyValueFactory<>("myBid"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("endTime"));

        // Dữ liệu mẫu
        ObservableList<AuctionHistory> data = FXCollections.observableArrayList(
                new AuctionHistory("1", "Rare Fighter Card", "$1,200", "WON", "2024-03-10"),
                new AuctionHistory("2", "Epic Skin Pack", "$450", "OUTBID", "2024-03-12"),
                new AuctionHistory("3", "Premium Slot #5", "$2,000", "ACTIVE", "2024-04-30")
        );
        auctionTable.setItems(data);
    }


}