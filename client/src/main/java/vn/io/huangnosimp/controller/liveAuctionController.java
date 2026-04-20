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
import java.net.URL;
import java.util.ResourceBundle;

import static vn.io.huangnosimp.Manager.ViewManager.changeView;

public class liveAuctionController implements Initializable {
    @FXML private LineChart<Number, Number> lineChart;
    @FXML private NumberAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private Label myBidLabel;
    @FXML private Button btnIncrease;
    @FXML private Button btnDecrease;
    private long minCount = 100000;
    private Timeline holdTimer;
    private Runnable currentAction;
    private long parseLabeltoLong(String labelText){
        String cleanText = labelText.replace(".","").trim();
        return Long.parseLong(cleanText);
    }
    private String LongtoLabel(long value){
        String formatted = String.format("%,d", value);
        String finalValue = formatted.replace(",", ".").trim();
        return finalValue;
    }
    public void handleIncreaseButton(){
        long currentBid = parseLabeltoLong(myBidLabel.getText());
        currentBid+=minCount;
        myBidLabel.setText(LongtoLabel(currentBid));
    }
    public void handleDecreaseButton(){
        long currentBid = parseLabeltoLong(myBidLabel.getText());
        if(currentBid - minCount >= minCount){
            currentBid -= minCount;
            myBidLabel.setText(LongtoLabel(currentBid));
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

    @FXML
    private void onMouseReleased(MouseEvent event) {
        holdTimer.stop();
        currentAction = null;
    }    // Dừng lại ngay khi thả chuột
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
