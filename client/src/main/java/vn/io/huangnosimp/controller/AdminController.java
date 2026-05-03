package vn.io.huangnosimp.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import vn.io.huangnosimp.dto.response.AuctionCardDTO;
import vn.io.huangnosimp.dto.response.MemberDTO;
import vn.io.huangnosimp.network.service.AdminNetworkService;
import vn.io.huangnosimp.Manager.SocketManager; // Giả sử bạn lấy socket từ đây

import java.net.URL;
import java.util.ResourceBundle;

public class AdminController implements Initializable {

    @FXML private Label lblSystemRevenue;


    @FXML private TableView<MemberDTO> tableMembers;
    @FXML private TableColumn<MemberDTO, String> colMemberId;
    @FXML private TableColumn<MemberDTO, String> colMemberName;
    @FXML private TableColumn<MemberDTO, String> colMemberStatus;

    @FXML private TableColumn<MemberDTO, Void> colMemberAction;


    @FXML private TableView<AuctionCardDTO> tableAuctions;
    @FXML private TableColumn<AuctionCardDTO, String> colAuctionId;
    @FXML private TableColumn<AuctionCardDTO, String> colAuctionName;
    @FXML private TableColumn<AuctionCardDTO, Double> colAuctionPrice;

    @FXML private TableColumn<AuctionCardDTO, Void> colAuctionAction;

    private AdminNetworkService networkService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        networkService = new AdminNetworkService(SocketManager.getClient());

        setupTableColumns();
        loadAllData();
    }

    private void setupTableColumns() {
        // --- BẢNG THÀNH VIÊN ---
        colMemberId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId()));
        colMemberName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));
        colMemberStatus.setCellValueFactory(cellData -> {
            boolean isBanned = cellData.getValue().isBanned(); // Giả sử DTO của bạn có hàm này
            return new SimpleStringProperty(isBanned ? "Bị Khóa" : "Hoạt động");
        });

        // --- BẢNG ĐẤU GIÁ ---
        colAuctionId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAuctionId()));
        colAuctionName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProductName()));
        colAuctionPrice.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getCurrentPrice()).asObject());
    }

    private void loadAllData() {
        // 1. Load Doanh thu
        networkService.getSystemRevenueAsync().thenAccept(revenue -> {
            Platform.runLater(() -> lblSystemRevenue.setText("$" + String.format("%,.2f", revenue)));
        });

        // 2. Load Danh sách Member
        networkService.getAllMembersAsync().thenAccept(members -> {
            Platform.runLater(() -> {
                ObservableList<MemberDTO> memberData = FXCollections.observableArrayList(members);
                tableMembers.setItems(memberData);
            });
        });

        // 3. Load Danh sách Auction
        networkService.getAllAuctionsAsync().thenAccept(auctions -> {
            Platform.runLater(() -> {
                ObservableList<AuctionCardDTO> auctionData = FXCollections.observableArrayList(auctions);
                tableAuctions.setItems(auctionData);
            });
        });
    }



    private void setupActionButtons() {
        // Nút cho Member
        colMemberAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnAction = new Button();
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    MemberDTO member = getTableView().getItems().get(getIndex());
                    btnAction.setText(member.isBanned() ? "Unban" : "Ban");
                    btnAction.setStyle(member.isBanned() ? "-fx-background-color: #22c55e;" : "-fx-background-color: #ef4444;");
                    btnAction.setOnAction(e -> {
                        if (member.isBanned()) {
                            networkService.unlockMemberAsync(member.getId()).thenAccept(res -> loadAllData());
                        } else {
                            networkService.lockMemberAsync(member.getId()).thenAccept(res -> loadAllData());
                        }
                    });
                    setGraphic(btnAction);
                }
            }
        });
    }
}