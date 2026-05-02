package vn.io.huangnosimp.controller;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import javafx.event.ActionEvent;
import vn.io.huangnosimp.Manager.*;
import vn.io.huangnosimp.dto.request.*;
import vn.io.huangnosimp.dto.response.*;
import vn.io.huangnosimp.network.IServerMessageListener;
import vn.io.huangnosimp.network.SocketClient;
import vn.io.huangnosimp.protocol.ActionType;
import vn.io.huangnosimp.protocol.Request;
import vn.io.huangnosimp.protocol.Response;
import vn.io.huangnosimp.protocol.ResponseStatus;
import vn.io.huangnosimp.util.GsonParser;

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

    @FXML private Label myBidLabel;
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

    @FXML private Button btnIncrease;
    @FXML private Button btnDecrease;
    @FXML private Button AutoBid;
    @FXML private Button btnPlaceBid;
    @FXML private Button btnBuyNow;
    @FXML private HBox Hbox1;
    @FXML private VBox Vbox1;
    @FXML private Separator spr;
    @FXML private Separator spr1;
    @FXML ListView bidHistoryList;

    private double minCount = 100000;
    private Timeline holdTimer;
    private Runnable currentAction;
    private String auctionId;

    private void setUpliveAuction(AuctionDetailResponseDTO DTO){
        currentPriceLabel.setText(FormatUtil.formatNumber(DTO.getCurrentPrice()));
        startPriceLabel.setText("Khởi điểm: "+FormatUtil.formatNumber(DTO.getStartPrice()));
        leadBidderLabel.setText(DTO.getLeadBidder());
        minNextBidLabel.setText(FormatUtil.formatNumber(DTO.getMinNextBid()));
        myBidLabel.setText(FormatUtil.formatNumber(DTO.getMinNextBid()));
        AuctionCountdownUtil clock = new AuctionCountdownUtil(timeLabel, DTO.getStartTime(), DTO.getEndTime());
        clock.start();
        bidStepLabel.setText(caculateBidIncreament(DTO.getBidIncrement()));
        bidIndex=DTO.getBidCount();
        bidCountLabel.setText(String.valueOf(DTO.getBidCount()));
        participantCountLabel.setText(String.valueOf(DTO.getParticipantCount()));

        loadChartHistory(DTO.getPriceHistory());
        loadBidHistory(DTO.getBidHistory());
        productTitleLabel.setText(DTO.getProductName());
        categoryLabel.setText(String.valueOf(DTO.getCategory()));
        conditionLabel.setText(String.valueOf(DTO.getCondition()));
        descriptionLabel.setText(DTO.getDescription());
        sidebarBuyNowPriceLabel.setText(formatNumber(DTO.getBuyNowPrice()));
        startDateLabel.setText(formatEpochSecond(DTO.getStartTime()));
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
    public void handlebtnWithdraw(ActionEvent event){
        changeView("dashboard_home.fxml", 1);
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
        btnBuyNow.setVisible(false);
        AutoBid.setText("Cancel Auction");
    }
    public void setupLineChart(){
        lineChart.setAnimated(false);
        lineChart.setCreateSymbols(false);
        lineChart.setLegendVisible(false);
        lineChart.setTitle("Auction");
        xAxis.setLabel("Lượt đặt");
        xAxis.setAutoRanging(true);
        xAxis.setTickLabelsVisible(false);

        yAxis.setLabel("Giá (đ)");
        yAxis.setAutoRanging(true);

        priceSeries.setName("Price");
        lineChart.getData().add(priceSeries);
    }
    public void loadChartHistory(List<PricePointDTO> history){
        priceSeries.getData().clear();
        bidIndex = 0;
        for(PricePointDTO dto : history){
            bidIndex++;
            priceSeries.getData().add(new XYChart.Data<>(bidIndex, dto.getPrice()));
        }
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
    public void handleBuyNowButton(){
        BuyNowRequestDTO buyNowRequest = new BuyNowRequestDTO(auctionId);
        Request request = new Request(ActionType.BUY_NOW, auctionId);
        btnBuyNow.setDisable(true);
        SocketManager.getClient().sendRequestAsync(request)
                .thenAccept(response -> {
                    if(ResponseStatus.SUCCESS.equals(response.getStatus())){
                        changeView("dashboard_home.fxml", 1);
                        ControllerManager.getDashboardController().showToast("SUCCESS", response.getMessage(), true);
                    }
                    if(ResponseStatus.FAILED.equals(response.getStatus())){
                        ControllerManager.getDashboardController().showToast("FAILED", response.getMessage(), false);
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
        if(AutoBid.getText().equals("Auto Bid")){

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
                                ControllerManager.getDashboardHomeController().removeFromDashBoard(this.auctionId, ControllerManager.getDashboardHomeController().getFlowJoined());
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
    public void setAuctionId(String id){
        this.auctionId = id;
    }
    public void onResponseReceived(Response response){
    }
    public void onResponseReceived(Response response, ActionType actionType){}
    public void onRequestReceived(Request notification){
        PlaceBidResponseDTO dto = GsonParser.GSON.fromJson(GsonParser.GSON.toJsonTree(notification.getData()), PlaceBidResponseDTO.class);
        bidIndex++;
        bidCountLabel.setText(String.valueOf(bidIndex));
        leadBidderLabel.setText(dto.getUsername());
        priceSeries.getData().add(new XYChart.Data<>(bidIndex, dto.getAmount()));
        currentPriceLabel.setText(formatNumber(dto.getAmount()));
        minNextBidLabel.setText(formatNumber(dto.getAmount()+minCount));
        BidHistoryDTO historyDTO = new BidHistoryDTO(dto.getUsername(), dto.getAmount(), dto.getPlaceAt());
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/bid_history_cell.fxml"));
            Parent cell = loader.load();
            BidHistoryCellController controller = loader.getController();
            controller.setData(historyDTO, true);
            bidHistoryList.getItems().add(cell);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
    public void onDisconnected(String reason){}
    @FXML
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        SocketManager.getClient().addListener(this);
        setupLineChart();
        holdTimer = new Timeline(new KeyFrame(Duration.millis(200), event -> {
            handleIncreaseButton(); // Gọi lại hàm tăng số bạn đã viết
        }));
        holdTimer.setCycleCount(Animation.INDEFINITE); // Chạy vô hạn cho đến khi thả chuột
        setUpliveAuction(UserSession.getAuctionDetail());
    }
}
