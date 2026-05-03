package vn.io.huangnosimp.controller;

import auction.info.AuctionInfo;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import javafx.event.ActionEvent;
import vn.io.huangnosimp.Manager.ControllerManager;
import vn.io.huangnosimp.dto.response.AuctionCardDTO;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static vn.io.huangnosimp.Manager.UserSession.getDashboardInfo;
import static vn.io.huangnosimp.Manager.ViewManager.*;


public class DashboardController extends BaseController {
    @FXML private BorderPane mainBorderPane;
    @FXML private StackPane contentArea; // Cái này nằm ở file dashboard.fxml nên giữ lại
    @FXML private Button createButton;
    @FXML private Button btnOpenSlots;
    @FXML private HBox menuHbox;
    @FXML private VBox sideVbox;
    @FXML private Label lblBalance;
    @FXML private Label lblActiveBids;
    @FXML private Label lblWinning;
    @FXML private Label lblOutbid;
    @FXML private Label lblWonTotal;

    @FXML
    public void handleMenuAction(ActionEvent event){
        Button clickedButton = (Button) event.getSource();

        for(Node node : menuHbox.getChildren()){
            if(node instanceof Button){
                Button btn = (Button) node;
                btn.getStyleClass().remove("nav-btn-active");
            }
        }
        clickedButton.getStyleClass().add("nav-btn-active");
    }
    @FXML
    public void handlebtnAvatar(ActionEvent event){
        changeView("AccountView.fxml", 1);
    }

    @FXML
    public void handlebtnDashboard(ActionEvent event){
        changeView("dashboard_home.fxml", 1);
        handleMenuAction(event);
    }

    @FXML
    public void handlebtnOpenSlots(ActionEvent event){
        changeView("open_slots.fxml", 1);
        handleMenuAction(event);
    }

    @FXML
    public void handleCreateClick(ActionEvent event){
        changeView("create_auction.fxml",2);
    }

    public Button getCreateButton(){
        return this.createButton;
    }

    @Override
    public void onInit(URL location, ResourceBundle resources){
        setMainBorderPane(mainBorderPane);
        ControllerManager.setDashboardController(this);
        changeView("dashboard_home.fxml", 1);
        lblBalance.setText(String.valueOf(getDashboardInfo().getBalance()));
        lblActiveBids.setText(String.valueOf(getDashboardInfo().getJoinedRooms()));
        lblOutbid.setText(String.valueOf(getDashboardInfo().getOutBids()));
        lblWinning.setText(String.valueOf(getDashboardInfo().getWinningBids()));
        lblWonTotal.setText(String.valueOf(getDashboardInfo().getWonTotal()));
    }

    @FXML
    private TextField txtSearch;

    @FXML
    private FlowPane auctionContainer;

    private List<AuctionCardDTO> allAuctions = new ArrayList<>();

    @FXML
    public void initialize() {
        txtSearch.setPromptText("Tìm kiếm phiên đấu giá...");
        auctionContainer.getChildren().clear();
    }

    public void setAllAuctions(List<AuctionCardDTO> auctions) {
        this.allAuctions = (auctions != null) ? auctions : new ArrayList<>();
        renderAuctionList(this.allAuctions);
    }

    @FXML
    void onSearchEnter(ActionEvent event) {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            loadAllAuctions();
        } else {
            handleFiltering(keyword);
        }
        txtSearch.getParent().requestFocus();
    }

    private void handleFiltering(String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        List<AuctionCardDTO> filteredList = allAuctions.stream()
                .filter(dto -> dto.getProductName().toLowerCase().contains(lowerKeyword) ||
                        dto.getAuctionId().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());

        if (filteredList.isEmpty()) {
            System.out.println("Không có phiên nào khớp với: " + keyword);
        }

        renderAuctionList(filteredList);
    }

    private void loadAllAuctions() {
        renderAuctionList(allAuctions);
    }

    private void renderAuctionList(List<AuctionCardDTO> results) {
        auctionContainer.getChildren().clear();
        for (AuctionCardDTO dto : results) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ItemCard.fxml"));
                Node cardNode = loader.load();
                ItemCardController controller = loader.getController();
                controller.addInfo(dto);

                auctionContainer.getChildren().add(cardNode);
            } catch (IOException e) {
                System.err.println("Lỗi render ItemCard: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public List<AuctionCardDTO> filterAuctions(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return allAuctions;

        String lower = keyword.toLowerCase();
        return allAuctions.stream()
                .filter(a -> a.getProductName().toLowerCase().contains(lower) ||
                        a.getAuctionId().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }
}
