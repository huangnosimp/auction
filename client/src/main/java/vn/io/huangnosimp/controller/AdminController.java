package vn.io.huangnosimp.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import vn.io.huangnosimp.dto.response.AuctionAdminDTO;
import vn.io.huangnosimp.dto.response.AuctionCardDTO;
import vn.io.huangnosimp.dto.response.MemberDTO;
import vn.io.huangnosimp.network.service.AdminNetworkService;
import vn.io.huangnosimp.Manager.SocketManager;

import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.ResourceBundle;


import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class AdminController implements Initializable {

    @FXML private Label lblSystemRevenue;

    @FXML private TextField txtBanDuration;

    @FXML private TableView<MemberDTO> tableMembers;
    @FXML private TableColumn<MemberDTO, String> colMemberId;
    @FXML private TableColumn<MemberDTO, String> colMemberName;
    @FXML private TableColumn<MemberDTO, String> colMemberBalance;
    @FXML private TableColumn<MemberDTO, String> colMemberFrozen;
    @FXML private TableColumn<MemberDTO, String> colMemberStatus;



    @FXML private TableView<AuctionAdminDTO> tableAuctions;
    @FXML private TableColumn<AuctionAdminDTO, String> colAuctionId;
    @FXML private TableColumn<AuctionAdminDTO, String> colAuctionName;
    @FXML private TableColumn<AuctionAdminDTO, String> colAuctionPrice;
    @FXML private TableColumn<AuctionAdminDTO, String> colSellerName;
    @FXML private TableColumn<AuctionAdminDTO, String> colStartPrice;
    @FXML private TableColumn<AuctionAdminDTO, String> colStartTime;
    @FXML private TableColumn<AuctionAdminDTO, String> colEndTime;
    @FXML private TableColumn<AuctionAdminDTO, String> colStatus;
    @FXML private TableColumn<AuctionAdminDTO, String> colWinnerName;

    private AdminNetworkService networkService;

    private Timeline autoRefreshTimeline;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        networkService = new AdminNetworkService(SocketManager.getClient());
        setupTableColumns();
        loadAllData();

        autoRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(10), event -> {
            loadAllData();
        }));
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE); // Chạy lặp đi lặp lại mãi mãi
        autoRefreshTimeline.play();

        lblSystemRevenue.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene == null) {
                stopAutoRefresh();
            }
        });
    }

    public void stopAutoRefresh() {
        if (autoRefreshTimeline != null) {
            autoRefreshTimeline.stop(); // Dừng Timeline
            System.out.println("Đã tắt tự động cập nhật dữ liệu.");
        }
    }

    private void setupTableColumns() {
        // BẢNG THÀNH VIÊN
        colMemberId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId()));
        colMemberName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));

        colMemberStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));

        colMemberStatus.setCellFactory(column -> new TableCell<MemberDTO, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);

                    if (item.contains("Online")) {
                        setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;"); // Xanh lá
                    } else if (item.contains("Bị Ban")) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;"); // Đỏ
                    } else {
                        setStyle("-fx-text-fill: #7f8c8d;"); // Xám
                    }
                }
            }
        });

        colMemberBalance.setCellValueFactory(cellData -> {
            double balance = cellData.getValue().getBalance();
            return new SimpleStringProperty("$" + String.format(Locale.US, "%,.0f", balance));
        });
        colMemberBalance.setStyle("-fx-alignment: CENTER_RIGHT;");

        colMemberFrozen.setCellValueFactory(cellData -> {
            double frozen = cellData.getValue().getFrozenBalance();
            return new SimpleStringProperty("$" + String.format(Locale.US, "%,.0f", frozen));
        });
        colMemberFrozen.setStyle("-fx-alignment: CENTER_RIGHT; -fx-text-fill: #e67e22;");

        colAuctionId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId()));
        colAuctionName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getItemName()));

        colAuctionPrice.setCellValueFactory(cellData -> {
            double price = cellData.getValue().getCurrentPrice();
            return new SimpleStringProperty("$" + String.format(Locale.US, "%,.0f", price));
        });
        colAuctionPrice.setStyle("-fx-alignment: CENTER_RIGHT;");

        colSellerName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSellerName()));

        colStartPrice.setCellValueFactory(cellData -> {
            double price = cellData.getValue().getStartingPrice();
            return new SimpleStringProperty("$" + String.format(Locale.US, "%,.0f", price));
        });
        colStartPrice.setStyle("-fx-alignment: CENTER_RIGHT;");

        colStartTime.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStartTime()));
        colEndTime.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEndTime()));
        colStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
        colWinnerName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getWinnerName()));

        tableMembers.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableAuctions.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void loadAllData() {
        // 1. Load Doanh thu
        networkService.getSystemRevenueAsync().thenAccept(revenue -> {
            Platform.runLater(() -> lblSystemRevenue.setText("$" + String.format("%,.2f", revenue)));
        });

        // 2. Load Danh sách Member và đấu giá, đồng thời giữ nguyên dòng đang được chọn nếu có
        networkService.getAllMembersAsync().thenAccept(members -> {
            Platform.runLater(() -> {
                // Lưu lại ID của member đang được chọn
                MemberDTO selectedMember = tableMembers.getSelectionModel().getSelectedItem();
                String selectedMemberId = (selectedMember != null) ? selectedMember.getId() : null;

                // Cập nhật dữ liệu mới vào bảng
                ObservableList<MemberDTO> memberData = FXCollections.observableArrayList(members);
                tableMembers.setItems(memberData);

                // Tìm và bôi xanh lại dòng cũ
                if (selectedMemberId != null) {
                    for (MemberDTO m : memberData) {
                        if (m.getId().equals(selectedMemberId)) {
                            tableMembers.getSelectionModel().select(m);
                            break;
                        }
                    }
                }
            });
        });

        networkService.getAllAuctionsAsync().thenAccept(auctions -> {
            Platform.runLater(() -> {
                // Lưu lại ID của auction đang được chọn
                AuctionAdminDTO selectedAuction = tableAuctions.getSelectionModel().getSelectedItem();
                String selectedAuctionId = (selectedAuction != null) ? selectedAuction.getId() : null;

                // Cập nhật dữ liệu mới vào bảng
                ObservableList<AuctionAdminDTO> auctionData = FXCollections.observableArrayList(auctions);
                tableAuctions.setItems(auctionData);

                // Tìm và bôi xanh lại dòng cũ
                if (selectedAuctionId != null) {
                    for (AuctionAdminDTO a : auctionData) {
                        if (a.getId().equals(selectedAuctionId)) {
                            tableAuctions.getSelectionModel().select(a);
                            break;
                        }
                    }
                }
            });
        });
    }

    @FXML
    public void handleBanMember() {
        MemberDTO selectedMember = tableMembers.getSelectionModel().getSelectedItem();
        if (selectedMember == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một thành viên từ bảng để khóa!");
            return;
        }

        int durationMinutes = -1; // Mặc định -1 là vĩnh viễn
        String input = txtBanDuration.getText().trim();

        if (!input.isEmpty()) {
            try {
                durationMinutes = Integer.parseInt(input);
                if (durationMinutes <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Số phút phải lớn hơn 0!");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập số hợp lệ!");
                return;
            }
        }

        final int finalDuration = durationMinutes;

        networkService.lockMemberAsync(selectedMember.getId(), durationMinutes).thenAccept(success -> {
            Platform.runLater(() -> {
                if (success) {
                    String msg = (finalDuration == -1) ? "Đã khóa vĩnh viễn!" : "Đã khóa " + finalDuration + " phút!";
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", msg);
                    txtBanDuration.clear(); // Xóa trắng ô nhập
                    loadAllData(); // Load lại dữ liệu bảng để cập nhật cột Status
                } else {
                    showAlert(Alert.AlertType.ERROR, "Thất bại", "Không thể khóa thành viên này.");
                }
            });
        });
    }

    @FXML
    public void handleUnbanMember() {
        MemberDTO selectedMember = tableMembers.getSelectionModel().getSelectedItem();
        if (selectedMember == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một thành viên từ bảng để mở khóa!");
            return;
        }

        // Gọi networkService xử lý (chạy ngầm)
        networkService.unlockMemberAsync(selectedMember.getId()).thenAccept(success -> {
            Platform.runLater(() -> {
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã mở khóa thành viên thành công!");
                    loadAllData(); // Load lại dữ liệu bảng
                } else {
                    showAlert(Alert.AlertType.ERROR, "Thất bại", "Không thể mở khóa thành viên này.");
                }
            });
        });
    }

    @FXML
    public void handleForceCancelAuction() {
        AuctionAdminDTO selectedAuction = tableAuctions.getSelectionModel().getSelectedItem();
        if (selectedAuction == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một phiên đấu giá để hủy!");
            return;
        }
        // hộp thoại xác nhận
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận");
        confirm.setHeaderText("Bạn có chắc chắn muốn hủy phiên đấu giá này?");
        confirm.setContentText("Sản phẩm: " + selectedAuction.getItemName());

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Chạy lệnh gửi qua mạng
                networkService.forceCancelAuctionAsync(selectedAuction.getId()).thenAccept(success -> {
                    Platform.runLater(() -> {
                        if (success) {
                            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã hủy phiên đấu giá!");
                            loadAllData(); // Tải lại bảng
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Thất bại", "Không thể hủy phiên đấu giá này.");
                        }
                    });
                });
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}