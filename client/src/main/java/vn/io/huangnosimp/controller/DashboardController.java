package vn.io.huangnosimp.controller;

import com.google.gson.reflect.TypeToken;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import javafx.event.ActionEvent;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import vn.io.huangnosimp.Manager.ControllerManager;
import vn.io.huangnosimp.Manager.SocketManager;
import vn.io.huangnosimp.Manager.UserSession;
import vn.io.huangnosimp.Manager.ViewManager;
import vn.io.huangnosimp.dto.request.GetPublicAcutionCardDTO;
import vn.io.huangnosimp.dto.request.GetPostedAuctionDTO;
import vn.io.huangnosimp.dto.request.GetEndedPostedAuctionDTO;
import vn.io.huangnosimp.dto.request.GetWonAuctionDTO;
import vn.io.huangnosimp.dto.response.GetPostedAuctionCardResponseDTO;
import vn.io.huangnosimp.dto.response.AuctionCardDTO;
import vn.io.huangnosimp.dto.response.GetPublicAuctionCardResponseDTO;
import vn.io.huangnosimp.dto.shared.NotificationDTO;
import vn.io.huangnosimp.enums.NotificationType;
import vn.io.huangnosimp.network.IServerMessageListener;
import vn.io.huangnosimp.protocol.ActionType;
import vn.io.huangnosimp.protocol.Request;
import vn.io.huangnosimp.protocol.Response;
import vn.io.huangnosimp.protocol.ResponseStatus;
import vn.io.huangnosimp.util.GsonParser;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.Instant;
import java.util.*;

import static vn.io.huangnosimp.Manager.FormatUtil.formatNumber;
import static vn.io.huangnosimp.Manager.UserSession.getDashboardInfo;
import static vn.io.huangnosimp.Manager.UserSession.getJoiningListCard;
import static vn.io.huangnosimp.Manager.ViewManager.*;


public class DashboardController implements Initializable, IServerMessageListener {
    @FXML private BorderPane mainBorderPane;
    @FXML private StackPane contentArea;
    @FXML private Button btnOpenSlots;
    @FXML private Button btnInventory;
    @FXML private HBox menuHbox;
    @FXML private VBox sideVbox;
    @FXML private Label lblBalance;

    @FXML private HBox toastBox;
    @FXML private Label toastTitle, toastSub, toastIconLabel;
    @FXML private Circle toastIconCircle;
    @FXML private ScrollPane outBidScrollPane;
    @FXML private VBox outbidAlertsList;
    @FXML private Label outbidBadge;

    public static DashboardController instance;

    @FXML
    public void handleMenuAction(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        for (Node node : menuHbox.getChildren()) {
            if (node instanceof Button btn) {
                btn.getStyleClass().remove("nav-btn-active");
            }
        }
        clickedButton.getStyleClass().add("nav-btn-active");
    }

    public Button getActiveMenuButton() {
        for (Node node : menuHbox.getChildren()) {
            if (node instanceof Button btn) {
                if (btn.getStyleClass().contains("nav-btn-active")) {
                    return btn;
                }
            }
        }
        return null;
    }

    @FXML
    public void handlebtnAvatar(ActionEvent event) {
        changeView("AccountView.fxml", 1);
        for (Node node : menuHbox.getChildren()) {
            if (node instanceof Button btn) {
                btn.getStyleClass().remove("nav-btn-active");
            }
        }
    }

    @FXML
    public void handlebtnDashboard(ActionEvent event) {
        changeView("dashboard_home.fxml", 1);
        handleMenuAction(event);
    }
    @FXML
    public void handlebtnInventory(ActionEvent event){
        showInventoryView();
    }

    public void showInventoryView() {
        Request activeListingsReq = new Request(ActionType.GET_POSTED_AUCTION_CARD, new GetPostedAuctionDTO(30));
        Request endedListingsReq = new Request(ActionType.GET_ENDED_POSTED_AUCTION, new GetEndedPostedAuctionDTO(30));
        Request wonReq = new Request(ActionType.GET_WON_AUCTION, new GetWonAuctionDTO(30));

        SocketManager.getClient().sendRequestAsync(activeListingsReq)
            .thenCombine(SocketManager.getClient().sendRequestAsync(endedListingsReq), (activeRes, endedRes) -> {
                List<AuctionCardDTO> activeList = new ArrayList<>();
                List<AuctionCardDTO> endedList = new ArrayList<>();
                if (activeRes != null && ResponseStatus.SUCCESS.equals(activeRes.getStatus())) {
                    GetPostedAuctionCardResponseDTO dto = GsonParser.GSON.fromJson(
                        GsonParser.GSON.toJsonTree(activeRes.getData()),
                        GetPostedAuctionCardResponseDTO.class
                    );
                    if (dto != null && dto.getAuctionCards() != null) {
                        activeList.addAll(dto.getAuctionCards());
                    }
                }
                if (endedRes != null && ResponseStatus.SUCCESS.equals(endedRes.getStatus())) {
                    List<AuctionCardDTO> list = GsonParser.GSON.fromJson(
                        GsonParser.GSON.toJsonTree(endedRes.getData()),
                        new TypeToken<List<AuctionCardDTO>>(){}.getType()
                    );
                    if (list != null) {
                        endedList.addAll(list);
                    }
                }
                List<AuctionCardDTO> allListings = new ArrayList<>();
                allListings.addAll(activeList);
                allListings.addAll(endedList);
                return allListings;
            })
            .thenCombine(SocketManager.getClient().sendRequestAsync(wonReq), (allListings, wonRes) -> {
                List<AuctionCardDTO> wonList = new ArrayList<>();
                if (wonRes != null && ResponseStatus.SUCCESS.equals(wonRes.getStatus())) {
                    List<AuctionCardDTO> list = GsonParser.GSON.fromJson(
                        GsonParser.GSON.toJsonTree(wonRes.getData()),
                        new TypeToken<List<AuctionCardDTO>>(){}.getType()
                    );
                    if (list != null) {
                        wonList.addAll(list);
                    }
                }
                
                Platform.runLater(() -> {
                    changeView("Inventory.fxml", 1);
                    InventoryController inventoryController = ControllerManager.getInventoryController();
                    if (inventoryController != null) {
                        inventoryController.setData(allListings, wonList);
                    }
                    
                    for (Node node : menuHbox.getChildren()) {
                        if (node instanceof Button btn) {
                            btn.getStyleClass().remove("nav-btn-active");
                        }
                    }
                    if (btnInventory != null) {
                        btnInventory.getStyleClass().add("nav-btn-active");
                    }
                });
                return null;
            }).exceptionally(ex -> {
                ex.printStackTrace();
                return null;
            });
    }

    @FXML
    public void handlebtnOpenSlots(ActionEvent event) {
        List<AuctionCardDTO> combineList = new ArrayList<>(UserSession.getJoiningListCard());
        combineList.addAll(UserSession.getMyListCard());

        List<AuctionCardDTO> result = new ArrayList<>();

        fetchUntilFull(result, combineList, 30, event, 5);
    }

    private void applyOpenSlots(List<AuctionCardDTO> result, ActionEvent event) {
        Platform.runLater(() -> {

            UserSession.addToList(result);

            if (ViewManager.getCache().containsKey("open_slots.fxml")) {
                ControllerManager.getOpenSlotController().clearContainer();
                ControllerManager.getOpenSlotController().setupOpenSlot(UserSession.getListCard());
            }

            ViewManager.changeView("open_slots.fxml", 1);
            handleMenuAction(event);
        });
    }

    private void fetchUntilFull(List<AuctionCardDTO> result,
                                List<AuctionCardDTO> excludeList,
                                int target,
                                ActionEvent event,
                                int maxRetry) {

        if (result.size() >= target || maxRetry <= 0) {
            applyOpenSlots(result, event);
            return;
        }

        int needed = target - result.size();
        GetPublicAcutionCardDTO requestDTO = new GetPublicAcutionCardDTO(needed * 2);
        Request request = new Request(ActionType.GET_PUBLIC_AUCTION_CARD, requestDTO);

        SocketManager.getClient().sendRequestAsync(request)
                .thenAccept(response -> {
                    if (!ResponseStatus.SUCCESS.equals(response.getStatus())) {
                        applyOpenSlots(result, event);
                        return;
                    }

                    GetPublicAuctionCardResponseDTO dto = GsonParser.GSON.fromJson(
                            GsonParser.GSON.toJsonTree(response.getData()),
                            GetPublicAuctionCardResponseDTO.class
                    );

                    List<AuctionCardDTO> responseList = dto.getPublicAuctionCardList();

                    if (responseList == null || responseList.isEmpty()) {
                        applyOpenSlots(result, event);
                        return;
                    }

                    Set<String> excludeIds = new HashSet<>();
                    excludeList.forEach(c -> excludeIds.add(c.getAuctionId()));
                    result.forEach(c -> excludeIds.add(c.getAuctionId()));

                    for (AuctionCardDTO card : responseList) {
                        long now = Instant.now().toEpochMilli();
                        if (!excludeIds.contains(card.getAuctionId()) && now <= card.getEndTime()) {
                            result.add(card);
                            excludeIds.add(card.getAuctionId());
                            if (result.size() >= target) break;
                        }
                    }

                    if (result.size() >= target) {
                        applyOpenSlots(result, event);
                    } else {
                        fetchUntilFull(result, excludeList, target, event, maxRetry - 1);
                    }
                });
    }

    public void showToast(String title, String sub, boolean success) {
        String borderColor = success ? "#22c55e" : "#e24b4a";
        String titleColor  = success ? "#4ade80" : "#f87171";
        String subColor    = success ? "#86efac" : "#fca5a5";
        String bgColor     = success ? "#1a2e1a" : "#2e1a1a";

        toastTitle.setText(title);
        toastSub.setText(sub);
        toastTitle.setStyle("-fx-text-fill: " + titleColor + "; -fx-font-size: 12px; -fx-font-weight: bold;");
        toastSub.setStyle("-fx-text-fill: " + subColor + "; -fx-font-size: 10px;");
        toastIconCircle.setFill(javafx.scene.paint.Color.web(borderColor));
        toastIconLabel.setText(success ? "✓" : "✕");
        toastBox.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-border-color: " + borderColor + ";" +
                        "-fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 10, 0, 0, 4);"
        );

        toastBox.setVisible(true);
        toastBox.setManaged(true);

        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> {
            FadeTransition fade = new FadeTransition(Duration.millis(400), toastBox);
            fade.setToValue(0);
            fade.setOnFinished(f -> {
                toastBox.setVisible(false);
                toastBox.setManaged(false);
                toastBox.setOpacity(1);
            });
            fade.play();
        });
        pause.play();
    }

    private void addOutbidCardToUI(){
        outbidAlertsList.getChildren().clear();
        for(AuctionCardDTO dto : getDashboardInfo().getAuctionCardInfo()){
            if(dto.getYourBid() < dto.getCurrentPrice()){
                try{
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/outbid_item.fxml"));
                    Parent node = loader.load();
                    OutbidItemController controller = loader.getController();
                    controller.setUp(dto);
                    outbidAlertsList.getChildren().add(node);
                    node.setUserData(dto.getAuctionId());
                }
                catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }
    public void removeOutbidItem(String Id){
        outbidAlertsList.getChildren().removeIf(node -> Id.equals(node.getUserData()));
        updateOutbidBadge();
    }

    public void addOrUpdateOutbidItem(String auctionId, String productName, double currentPrice) {
        Platform.runLater(() -> {
            removeOutbidItem(auctionId);
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/outbid_item.fxml"));
                Parent node = loader.load();
                OutbidItemController controller = loader.getController();
                controller.setUpManual(auctionId, productName, currentPrice);
                outbidAlertsList.getChildren().add(node);
                node.setUserData(auctionId);
                updateOutbidBadge();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private double parsePriceFromMessage(String msg, double defaultValue) {
        if (msg == null) return defaultValue;
        try {
            String prefix = "Current price is ";
            int idx = msg.indexOf(prefix);
            if (idx != -1) {
                int endIdx = msg.indexOf(" in room", idx + prefix.length());
                if (endIdx != -1) {
                    String priceStr = msg.substring(idx + prefix.length(), endIdx).trim();
                    return Double.parseDouble(priceStr);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    private void setupDashboard() {
        lblBalance.setText(formatNumber(calculateBalance(getDashboardInfo().getBalance()))+" VND");
        ControllerManager.getDashboardHomeController().setupDashboardHome();
        addOutbidCardToUI();
        updateOutbidBadge();
    }

    public void updateOutbidBadge() {
        int count = outbidAlertsList.getChildren().size();
        outbidBadge.setText(String.valueOf(count));
        outbidBadge.setVisible(count > 0);
        outbidBadge.setManaged(count > 0);
    }

    public void updateBalance(double amount){
        lblBalance.setText(formatNumber(calculateBalance(amount))+" VND");
    }
    private double calculateBalance(double value) {
        return Double.parseDouble(BigDecimal.valueOf(value).toPlainString());
    }

    @Override
    public void onResponseReceived(Response response){}
    public void onResponseReceived(Response response, ActionType actionType){}
    public void onRequestReceived(Request notification){
        NotificationDTO notificationDTO = GsonParser.GSON.fromJson(GsonParser.GSON.toJsonTree(notification.getData()), NotificationDTO.class);
        NotificationType type = notificationDTO.getNotificationType();
        switch (type){
            case OUTBID -> {
                Platform.runLater(() -> {
                    String auctionId = notificationDTO.getAuctionId();
                    String msg = notificationDTO.getData() != null ? notificationDTO.getData().toString() : "";
                    double currentPrice = parsePriceFromMessage(msg, 0.0);
                    
                    String productName = "Auction #" + auctionId;
                    for (AuctionCardDTO dto : getJoiningListCard()) {
                        if (auctionId.equals(dto.getAuctionId())) {
                            productName = dto.getProductName();
                            if (currentPrice <= 0.0) {
                                currentPrice = dto.getCurrentPrice();
                            }
                            break;
                        }
                    }
                    addOrUpdateOutbidItem(auctionId, productName, currentPrice);
                });
            }
            case AUCTION_ENDED -> {
                Platform.runLater(() -> {
                    removeOutbidItem(notificationDTO.getAuctionId());
                });
            }
            case AUCTION_CANCELED -> {
                Platform.runLater(() -> {
                    removeOutbidItem(notificationDTO.getAuctionId());
                });
            }
        }
    }
    public void onDisconnected(String reason){}

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setMainBorderPane(mainBorderPane);
        ControllerManager.setDashboardController(this);
        setupDashboard();
        changeView("dashboard_home.fxml", 1);
    }
}