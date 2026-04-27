package vn.io.huangnosimp.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

import javafx.event.ActionEvent;
import vn.io.huangnosimp.Manager.AuctionCountdownUtil;
import vn.io.huangnosimp.Manager.FormatUtil;
import vn.io.huangnosimp.dto.response.AuctionDetailResponseDTO;

import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import static vn.io.huangnosimp.Manager.FormatUtil.formatNumber;
import static vn.io.huangnosimp.Manager.FormatUtil.parseNumber;
import static vn.io.huangnosimp.Manager.ViewManager.changeView;

public class liveAuctionController implements Initializable {
    @FXML private LineChart<Number, Number> lineChart;
    @FXML private NumberAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private Label myBidLabel;
    @FXML private Label timeLabel;
    @FXML private Label currentPriceLabel;
    @FXML private Label startPriceLabel;
    @FXML private Label leadBidderLabel;
    @FXML private Label minNextBidLabel;
    @FXML private Label bidStepLabel;
    @FXML private Label bidCountLabel;
    @FXML private Label participantCountLabel;

    @FXML private Button btnIncrease;
    @FXML private Button btnDecrease;
    private double minCount;
    private Timeline holdTimer;
    private Runnable currentAction;


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
            return value/1000000000+" Billions";
        }
        else if (value>1000000){
            return value/1000000+" Millions";
        }
        else {
            return String.valueOf(value);
        }
    }
    @FXML
    private void onMouseReleased(MouseEvent event) {
        holdTimer.stop();
        currentAction = null;
    }    // Dừng lại ngay khi thả chuột
    private void setUpliveAuction(AuctionDetailResponseDTO DTO){
        currentPriceLabel.setText(FormatUtil.formatNumber(DTO.getCurrentPrice())+"₫");
        startPriceLabel.setText("Khởi điểm: "+FormatUtil.formatNumber(DTO.getStartPrice())+"₫");
        leadBidderLabel.setText(DTO.getLeadBidder());
        minNextBidLabel.setText(FormatUtil.formatNumber(DTO.getMinNextBid())+"₫");
        myBidLabel.setText(FormatUtil.formatNumber(DTO.getMinNextBid())+"₫");
        AuctionCountdownUtil clock = new AuctionCountdownUtil(timeLabel, DTO.getStartTime(), DTO.getEndTime());
        clock.start();
        bidStepLabel.setText(caculateBidIncreament(DTO.getBidIncrement()));
        bidCountLabel.setText(String.valueOf(DTO.getBidCount()));
        participantCountLabel.setText(String.valueOf(DTO.getParticipantCount()));
        minCount = DTO.getBidIncrement();
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        xAxis.setLabel("Year");
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(2020);
        xAxis.setUpperBound(2026);
        xAxis.setTickUnit(1);

        yAxis.setLabel("Sales in Dollars");
        yAxis.setAutoRanging(true);

        lineChart.setTitle("Auction");
        lineChart.setData(SalesData.getSalesData());
        holdTimer = new Timeline(new KeyFrame(Duration.millis(200), event -> {
            handleIncreaseButton(); // Gọi lại hàm tăng số bạn đã viết
        }));
        holdTimer.setCycleCount(Animation.INDEFINITE); // Chạy vô hạn cho đến khi thả chuột

    }
}
