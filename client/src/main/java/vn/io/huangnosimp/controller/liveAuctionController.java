package vn.io.huangnosimp.controller;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import javafx.event.ActionEvent;
import vn.io.huangnosimp.Manager.*;
import vn.io.huangnosimp.dto.request.*;
import vn.io.huangnosimp.dto.response.*;
import vn.io.huangnosimp.dto.shared.NotificationDTO;
import vn.io.huangnosimp.enums.NotificationType;
import vn.io.huangnosimp.network.IServerMessageListener;
import vn.io.huangnosimp.network.SocketClient;
import vn.io.huangnosimp.protocol.ActionType;
import vn.io.huangnosimp.protocol.Request;
import vn.io.huangnosimp.protocol.Response;
import vn.io.huangnosimp.protocol.ResponseStatus;
import vn.io.huangnosimp.util.GsonParser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import static vn.io.huangnosimp.Manager.AuctionCountdownUtil.formatEpochSecond;
import static vn.io.huangnosimp.Manager.FormatUtil.formatNumber;
import static vn.io.huangnosimp.Manager.FormatUtil.parseNumber;
import static vn.io.huangnosimp.Manager.ViewManager.changeView;

public class liveAuctionController implements Initializable, IServerMessageListener {
    @FXML private LineChart<Number, Number> lineChart;
    @FXML private NumberAxis xAxis;
    @FXML private NumberAxis yAxis;
    private XYChart.Series<Number, Number> priceSeries = new XYChart.Series<>();
    private int bidIndex = 0;

    @FXML private TextField myBidLabel;
    @FXML private Label timeLabel;
    @FXML private Label currentPriceLabel;
    @FXML private Label startPriceLabel;
    @FXML private Label leadBidderLabel;
    @FXML private Label minNextBidLabel;
    @FXML private Label bidStepLabel;
    @FXML private Label bidCountLabel;
    @FXML private Label participantCountLabel;
    @FXML private Label productTitleLabel;
    @FXML private Label categoryLabel;
    @FXML private Label conditionLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label sidebarBuyNowPriceLabel;
    @FXML private Label startDateLabel;
    @FXML private Label autoBidStatusLabel;
    @FXML private Label lastBidTimeLabel;
    @FXML private Label statusLabel;

    @FXML private Button btnIncrease;
    @FXML private Button btnDecrease;
    @FXML private Button AutoBid;
    @FXML private Button btnPlaceBid;
    @FXML private Button btnBuyNow;
    @FXML private Button btnLeaveRoom;
    @FXML private HBox Hbox1;
    @FXML private VBox Vbox1;
    @FXML private VBox autoBidVbox;
    @FXML private VBox buyNowDisplay;
    @FXML private Separator spr;
    @FXML private Separator spr1;
    @FXML private ListView bidHistoryList;
    @FXML private TextField maxBidInput;
    @FXML private HBox AutobidHbox;
    @FXML private VBox AutobidVbox;

    @FXML private FlowPane imgDisplayPlowPane;
    @FXML private StackPane imageOverlay;
    @FXML private ImageView imgOverlayView;


    private double minCount;
    private Timeline holdTimer;
    private Runnable currentAction;
    private String auctionId;
    private boolean isBuyNowDp;

    public void setUpPreviewImg(List<String> Img) {
        if (Img == null || Img.isEmpty()) return;

        Platform.runLater(() -> {
            imgDisplayPlowPane.getChildren().clear();
            for (String picture : Img) {
                ImageView iv = new ImageView(new Image(picture, 120, 120, true, true));
                iv.setFitWidth(120);
                iv.setFitHeight(120);
                iv.setPreserveRatio(true);
                iv.setCursor(Cursor.HAND);
                iv.setOnMouseClicked(e -> openImageOverlay(picture));
                imgDisplayPlowPane.getChildren().add(iv);
            }
        });
    }
    private void zoomImage(){
        final double ZOOM_FACTOR = 1.1;
        imgOverlayView.setOnScroll(scrollEvent -> {
            if(scrollEvent.getDeltaY() > 0){
                imgOverlayView.setScaleX(imgOverlayView.getScaleX() * ZOOM_FACTOR);
                imgOverlayView.setScaleY(imgOverlayView.getScaleY() * ZOOM_FACTOR);
            }
            else{
                if (imgOverlayView.getScaleX() > 1.0) {
                    imgOverlayView.setScaleX(imgOverlayView.getScaleX() / ZOOM_FACTOR);
                    imgOverlayView.setScaleY(imgOverlayView.getScaleY() / ZOOM_FACTOR);
                }
            }
            scrollEvent.consume();
        });
    }
    private void openImageOverlay(String url){
        imgOverlayView.setImage(new Image(url, true));
        imageOverlay.setVisible(true);
        imageOverlay.setManaged(true);
    }

    @FXML
    private void closeImageOverlay(){
        imageOverlay.setVisible(false);
        imageOverlay.setManaged(false);
    }


    private void setUpliveAuction(AuctionDetailResponseDTO DTO){
        currentPriceLabel.setText(FormatUtil.formatNumber(DTO.getCurrentPrice()));//giá hiện tại
        startPriceLabel.setText("Khởi điểm: "+FormatUtil.formatNumber(DTO.getStartPrice()));//giá khởi điểm
        leadBidderLabel.setText(DTO.getLeadBidder() != null ? DTO.getLeadBidder() : "No bids yet");//người đang dẫn đầu
        if(DTO.getLastBidTime()==DTO.getStartTime()){
            lastBidTimeLabel.setText("—");
        }
        else {
            lastBidTimeLabel.setText(formatEpochSecond(DTO.getLastBidTime()));//thời gian đặt giá gần nhất
        }
        minNextBidLabel.setText(FormatUtil.formatNumber(DTO.getMinNextBid()));//giá kế tiếp tối thiểu
        myBidLabel.setText(FormatUtil.formatNumber(DTO.getMinNextBid()));//bảng chọn giá
        AuctionCountdownUtil clock = new AuctionCountdownUtil(timeLabel, DTO.getStartTime(), DTO.getEndTime());//đồng hồ đếm ngược
        clock.start();//bắt đầu đếm ngược
        bidStepLabel.setText(caculateBidIncreament(DTO.getBidIncrement()));//bước giá
        minCount=DTO.getBidIncrement();//lưu bước giá
        bidIndex=DTO.getBidCount();//lưu số lượng bid đã đặt
        bidCountLabel.setText(String.valueOf(DTO.getBidCount()));//số lượt đặt giá
        participantCountLabel.setText(String.valueOf(DTO.getParticipantCount()));//số người tham gia

        loadChartHistory(DTO.getPriceHistory());//load biểu đồ
        loadBidHistory(DTO.getBidHistory());//load lịch sử đặt bid
        productTitleLabel.setText(DTO.getProductName());//tên sản phẩm
        categoryLabel.setText(String.valueOf(DTO.getCategory()));//phân loại
        conditionLabel.setText(String.valueOf(DTO.getCondition()));//tình trạng
        descriptionLabel.setText(DTO.getDescription());//mô tả
        sidebarBuyNowPriceLabel.setText(formatNumber(DTO.getBuyNowPrice()));//giá mua ngay
        startDateLabel.setText(formatEpochSecond(DTO.getStartTime()));//thời điểm bắt đầu
        if(DTO.getBuyNowPrice() == 0 || DTO.getCurrentPrice() >= DTO.getBuyNowPrice()){
            buyNowDisplay.setVisible(false);
            buyNowDisplay.setManaged(false);
        }
        if(DTO.getLeadBidder().equals(UserSession.getUsername()) && DTO.getLeadBidder() != null){//chặn người đứng đầu đặt bid
            btnPlaceBid.setDisable(true);
            statusLabel.setText("WINNING");
        }
        else if (leadBidderLabel.getText().equals("No bids yet")) {
            statusLabel.setText("_____");
        }
        else{
            statusLabel.setText("OUTBID");
        }
    }
    public void handleIncreaseButton(){
        double currentBid = parseNumber(myBidLabel.getText());
        currentBid+=minCount;
        myBidLabel.setText(formatNumber(currentBid));
    }

    public void handleDecreaseButton(){
        double currentBid = parseNumber(myBidLabel.getText());
        if(currentBid - minCount >= minCount){
            currentBid -= minCount;
            myBidLabel.setText(formatNumber(currentBid));
        }
        else{
            holdTimer.stop();
        }
    }

    @FXML
    private void onMousePressed(MouseEvent event) {
        if(event.getSource() == btnIncrease){
            currentAction = this::handleIncreaseButton;
        }
        else if (event.getSource() == btnDecrease) {
            currentAction = this::handleDecreaseButton;
        }
        if(currentAction!=null){
            holdTimer.playFromStart();
        }
    }

    private String caculateBidIncreament(double value){
        if(value > 1000000000){
            return value/1000000000+" B";
        }
        else if (value>1000000){
            return value/1000000+" M";
        }
        else {
            return String.valueOf(value);
        }
    }

    @FXML
    private void onMouseReleased(MouseEvent event) {
        holdTimer.stop();
        currentAction = null;
    }

    public void setInvisible(){
        Hbox1.setVisible(false);
        Vbox1.setVisible(false);
        spr.setVisible(false);
        spr1.setVisible(false);
        btnPlaceBid.setVisible(false);
        btnLeaveRoom.setVisible(false);
        btnBuyNow.setVisible(false);
        btnBuyNow.setManaged(false);
        autoBidVbox.setVisible(false);
        autoBidVbox.setManaged(false);
        AutoBid.setText("Cancel Auction");
    }

    public void setupLineChart(){
        lineChart.setAnimated(false);
        lineChart.setCreateSymbols(false);
        lineChart.setLegendVisible(false);
        lineChart.setTitle("Auction");
        xAxis.setLabel("Lượt đặt");
        xAxis.setAutoRanging(false);
        xAxis.setTickLabelsVisible(true);
        xAxis.setMinorTickVisible(false);
        xAxis.setLowerBound(1);
        xAxis.setUpperBound(10);
        xAxis.setTickUnit(1);

        yAxis.setLabel("Giá (đ)");
        yAxis.setAutoRanging(true);

        priceSeries.setName("Price");
        lineChart.getData().add(priceSeries);
    }

    private void updateXAxisRange() {
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(1);
        int maxIndex = Math.max(10, bidIndex);
        xAxis.setUpperBound(maxIndex);
        xAxis.setTickUnit(1);
    }

    public void loadChartHistory(List<PricePointDTO> history){
        priceSeries.getData().clear();
        bidIndex = 0;
        for(PricePointDTO dto : history){
            bidIndex++;
            priceSeries.getData().add(new XYChart.Data<>(bidIndex, dto.getPrice()));
        }
        updateXAxisRange();
    }

    public void loadBidHistory(List<BidHistoryDTO> history){
        for(BidHistoryDTO dto : history) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/bid_history_cell.fxml"));
                Parent cell = loader.load();
                BidHistoryCellController controller = loader.getController();
                controller.setData(dto, true);
                bidHistoryList.getItems().add(cell);
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }
    @FXML
    public void handleReturnToDashboard(){

        Button ActBtn = ControllerManager.getDashboardController().getActiveMenuButton();
        if(ActBtn.getText().equals("Dashboard")){
            changeView("dashboard_home.fxml", 1);
        }
        else{
            changeView("open_slots.fxml", 1);
            ControllerManager.getOpenSlotController().clearCard(auctionId);
        }
    }
    @FXML
    public void handleLeaveRoom(){
        LeaveRoomRequestDTO leaveRoomRequest = new LeaveRoomRequestDTO(auctionId);
        Request request = new Request(ActionType.LEAVE_ROOM, leaveRoomRequest);
        SocketManager.getClient().sendRequestAsync(request)
                .thenAccept(response -> {
                    if(ResponseStatus.SUCCESS.equals(response.getStatus())){
                        Platform.runLater(()->{
                            ControllerManager.getDashboardHomeController().removeFromDashBoard(auctionId, ControllerManager.getDashboardHomeController().getFlowJoined());
                            UserSession.removeCard(UserSession.getJoiningListCard(), auctionId);
                            Button activeBtn = ControllerManager.getDashboardController().getActiveMenuButton();
                            if(activeBtn.getText().equals("Dashboard")){
                                changeView("dashboard_home.fxml", 1);
                            }
                            else{
                                changeView("open_slots.fxml", 1);
                            }
                            ControllerManager.getDashboardController().showToast("SUCCESS", response.getMessage(), true);
                        });
                    }
                    else{
                        Platform.runLater(()->{
                            ControllerManager.getDashboardController().showToast("FAILED", response.getMessage(), false);
                        });
                    }
                });
    }

    @FXML
    public void handleBuyNowButton(){
        BuyNowRequestDTO buyNowRequest = new BuyNowRequestDTO(auctionId);
        Request request = new Request(ActionType.BUY_NOW, buyNowRequest);
        btnBuyNow.setDisable(true);
        SocketManager.getClient().sendRequestAsync(request)
                .thenAccept(response -> {
                    if(ResponseStatus.SUCCESS.equals(response.getStatus())){
                        Platform.runLater(()->{
                            changeView("dashboard_home.fxml", 1);
                            ControllerManager.getDashboardController().showToast("SUCCESS", response.getMessage(), true);
                            btnBuyNow.setDisable(false);
                        });
                    }
                    if(ResponseStatus.FAILED.equals(response.getStatus())){
                        Platform.runLater(()->{
                            ControllerManager.getDashboardController().showToast("FAILED", response.getMessage(), false);
                            btnBuyNow.setDisable(false);
                        });
                    }

                });
    }

    @FXML
    public void handlePlaceBid(){
        double amount = parseNumber(myBidLabel.getText());
        PlaceBidRequestDTO placeBidRequest = new PlaceBidRequestDTO(auctionId, amount);
        Request request = new Request(ActionType.PLACE_BID, placeBidRequest);
        SocketManager.getClient().sendRequest(request);
    }

    @FXML
    public void handleCancelAndAutobid(){
        if(AutoBid.getText().equals("Auto Bid")) {
            double maxBid = 0;
            try {
                maxBid = Double.parseDouble(maxBidInput.getText());
            } catch (NumberFormatException e) {
                ControllerManager.getDashboardController().showToast("FAILED", "Số tiền không hợp lệ", false);
            }
            AutoBidRequestDTO autoBidRequest = new AutoBidRequestDTO(auctionId, maxBid, minCount);
            Request request = new Request(ActionType.REGISTER_AUTO_BID, autoBidRequest);
            SocketManager.getClient().sendRequest(request);
        }
        else if(AutoBid.getText().equals("Cancel Auto Bid")){
            double maxBid = 0;
            try {
                maxBid = Double.parseDouble(maxBidInput.getText());
            } catch (NumberFormatException e) {
                ControllerManager.getDashboardController().showToast("FAILED", "Số tiền không hợp lệ", false);
            }
            AutoBidRequestDTO cancelAutoBidRequest = new AutoBidRequestDTO(auctionId, maxBid, minCount);
            Request stopAutobid = new Request(ActionType.UNREGISTER_AUTO_BID, cancelAutoBidRequest);
            SocketManager.getClient().sendRequestAsync(stopAutobid)
                    .thenAccept(response -> {
                        if(ResponseStatus.SUCCESS.equals(response.getStatus())){
                            updateAutoBidStatus(false);
                        }
                        else{
                            ControllerManager.getDashboardController().showToast("FAILED", response.getMessage(), false);
                        }
                    });
        }
        else{
            CancelAuctionRequestDTO cancelAuctionRequest = new CancelAuctionRequestDTO(auctionId);
            Request request = new Request(ActionType.CANCEL_AUCTION, cancelAuctionRequest);
            AutoBid.setDisable(true);
            SocketManager.getClient().sendRequestAsync(request)
                    .thenAccept(cancelResponse->{
                        if("SUCCESS".equals(String.valueOf(cancelResponse.getStatus()))){
                            Platform.runLater(()->{
                                ControllerManager.getDashboardController().showToast("SUCCESS", cancelResponse.getMessage(), true);
                                ControllerManager.getDashboardHomeController().removeFromDashBoard(this.auctionId, ControllerManager.getDashboardHomeController().getAuctionFlowPane());
                                UserSession.removeCard(UserSession.getMyListCard(), auctionId);
                                ViewManager.changeView("dashboard_home.fxml", 1);
                            });
                        }
                        else {
                            Platform.runLater(()->{
                                ControllerManager.getDashboardController().showToast("FAILED", cancelResponse.getMessage(), false);
                            });
                        }
                    });
        }
    }
    private void updateAutoBidStatus(boolean active){
        autoBidVbox.getStyleClass().removeAll("autobid-inactive", "autobid-active");
        autoBidStatusLabel.getStyleClass().removeAll("autobid-status-inactive", "autobid-status-active");
        AutoBid.getStyleClass().removeAll("bidbtn", "autobid-cancel-btn");
        if (active) {
            autoBidVbox.getStyleClass().add("autobid-active");
            autoBidStatusLabel.getStyleClass().add("autobid-status-active");
            autoBidStatusLabel.setText("● Đang hoạt động");
            AutoBid.getStyleClass().add("autobid-cancel-btn");
            AutoBid.setText("Cancel Auto Bid");
            maxBidInput.setDisable(true);
        } else {
            autoBidVbox.getStyleClass().add("autobid-inactive");
            autoBidStatusLabel.getStyleClass().add("autobid-status-inactive");
            autoBidStatusLabel.setText("● Chưa kích hoạt");
            AutoBid.getStyleClass().add("bidbtn");
            AutoBid.setText("Auto Bid");
            maxBidInput.setDisable(false);
        }
    }
    public void setAuctionId(String id){
        this.auctionId = id;
    }
    public void onResponseReceived(Response response){
    }
    public void onResponseReceived(Response response, ActionType actionType){
        Platform.runLater(()->{
            switch (actionType){
                case PLACE_BID -> {
                    if(ResponseStatus.SUCCESS.equals(response.getStatus())) {
                        ControllerManager.getDashboardController().showToast("SUCCESS", response.getMessage(), true);
                        ControllerManager.getDashboardController().removeOutbidItem(auctionId);
                    }
                    else {
                        ControllerManager.getDashboardController().showToast("FAILED", response.getMessage(), false);
                    }
                }
                case REGISTER_AUTO_BID -> {
                    if(ResponseStatus.SUCCESS.equals(response.getStatus())){
                        AutoBid.setText("Cancel Auto Bid");
                        updateAutoBidStatus(true);
                        ControllerManager.getDashboardController().showToast("Active Auto Bid", response.getMessage(), true);
                    }
                    if(ResponseStatus.ERROR.equals(response.getStatus())){
                        ControllerManager.getDashboardController().showToast("ERROR", response.getMessage(), false);
                    }
                }
            }
        });
    }
    public void onRequestReceived(Request notification) {
        NotificationDTO notificationDTO = GsonParser.GSON.fromJson(GsonParser.GSON.toJsonTree(notification.getData()), NotificationDTO.class);
        NotificationType type = notificationDTO.getNotificationType();
        switch (type){
            case NEW_BID -> {
                PlaceBidResponseDTO dto = GsonParser.GSON.fromJson(GsonParser.GSON.toJsonTree(notificationDTO.getData()), PlaceBidResponseDTO.class);
                BidHistoryDTO historyDTO = new BidHistoryDTO(
                        dto.getUsername(), dto.getAmount(), dto.getPlaceAt()
                );

                Platform.runLater(() -> {
                    if (dto.getUsername().equals(UserSession.getUsername())) {
                        btnPlaceBid.setDisable(true);
                        statusLabel.setText("WINNING");
                        if (ControllerManager.getDashboardController() != null) {
                            ControllerManager.getDashboardController().removeOutbidItem(notificationDTO.getAuctionId());
                        }
                    } else {
                        btnPlaceBid.setDisable(false);
                        statusLabel.setText("OUTBID");
                        if (ControllerManager.getDashboardController() != null) {
                            String productName = UserSession.getAuctionDetail() != null ? UserSession.getAuctionDetail().getProductName() : "Auction #" + notificationDTO.getAuctionId();
                            ControllerManager.getDashboardController().addOrUpdateOutbidItem(notificationDTO.getAuctionId(), productName, dto.getAmount());
                        }
                    }
                    bidIndex++;
                    bidCountLabel.setText(String.valueOf(bidIndex));
                    leadBidderLabel.setText(dto.getUsername());
                    priceSeries.getData().add(new XYChart.Data<>(bidIndex, dto.getAmount()));
                    updateXAxisRange();
                    currentPriceLabel.setText(formatNumber(dto.getAmount()));
                    minNextBidLabel.setText(formatNumber(dto.getAmount() + minCount));
                    myBidLabel.setText(formatNumber(dto.getAmount() + minCount));
                    lastBidTimeLabel.setText(formatEpochSecond(dto.getPlaceAt()));

                    if (UserSession.getAuctionDetail() != null && UserSession.getAuctionDetail().getBuyNowPrice() > 0) {
                        if (dto.getAmount() >= UserSession.getAuctionDetail().getBuyNowPrice()) {
                            buyNowDisplay.setVisible(false);
                            buyNowDisplay.setManaged(false);
                        }
                    }

                    try {
                        FXMLLoader loader = new FXMLLoader(
                                getClass().getResource("/fxml/bid_history_cell.fxml")
                        );
                        Parent cell = loader.load();
                        BidHistoryCellController controller = loader.getController();
                        controller.setData(historyDTO, true);
                        bidHistoryList.getItems().add(0, cell);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
            case AUCTION_CANCELED -> {
                Platform.runLater(()->{
                    handleReturnToDashboard();
                    ControllerManager.getDashboardController().showToast("Auction has been canceled buy the seller", null, true);
                });
            }
            case AUCTION_ENDED -> {
                Platform.runLater(()->{
                    handleReturnToDashboard();
                    ControllerManager.getDashboardController().showToast("Auction Ended", null, true);
                });
            }
        }
    }

    public void onDisconnected(String reason){}
    @FXML
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        SocketManager.getClient().addListener(this);
        setAuctionId(UserSession.getAuctionId());
        setupLineChart();
        holdTimer = new Timeline(new KeyFrame(Duration.millis(200), event -> {
            handleIncreaseButton();
        }));
        holdTimer.setCycleCount(Animation.INDEFINITE);
        setUpliveAuction(UserSession.getAuctionDetail());
        zoomImage();
    }
}
