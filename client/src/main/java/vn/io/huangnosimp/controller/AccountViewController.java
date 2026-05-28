package vn.io.huangnosimp.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import vn.io.huangnosimp.Manager.ControllerManager;
import vn.io.huangnosimp.Manager.SocketManager;
import vn.io.huangnosimp.Manager.UserSession;
import vn.io.huangnosimp.Manager.ViewManager;
import vn.io.huangnosimp.dto.request.DepositWithdrawRequestDTO;
import vn.io.huangnosimp.protocol.ActionType;
import vn.io.huangnosimp.protocol.Request;
import vn.io.huangnosimp.protocol.ResponseStatus;

import java.net.URL;
import java.util.ResourceBundle;

import static vn.io.huangnosimp.Manager.ControllerManager.clearAllController;
import static vn.io.huangnosimp.Manager.ControllerManager.setAccountViewController;
import static vn.io.huangnosimp.Manager.UserSession.*;

public class AccountViewController implements Initializable {
    @FXML private HBox menuBar;
    @FXML private VBox paneTransactions;
    @FXML private VBox paneDeposit;
    @FXML private VBox paneWithdraw;
    @FXML private Button btnDeposit;
    @FXML private Button confirmDep;
    @FXML private Button btnWithdraw;
    @FXML private Button confirmWit;
    @FXML private TextField depAmount;
    @FXML private TextField witAmount;

    @FXML private Label usernameLabel;
    @FXML private Label emailLabel;

    private void setUpAccountView(){

        usernameLabel.setText(UserSession.getUsername());
        emailLabel.setText(UserSession.getEmail());
    }
    @FXML
    public void handleLogOut(){
        Platform.runLater(()->{
            ViewManager.getCache().clear();
            clearAllController();
            clearAllSession();
            ViewManager.changeMainStage("Login.fxml");
        });
    }
    @FXML
    public void handleEnterDeposit(){
        menuBar.setVisible(false);
        paneTransactions.setVisible(false);
        paneDeposit.setVisible(true);
        paneWithdraw.setVisible(false);
        menuBar.setManaged(false);
        paneTransactions.setManaged(false);
        paneDeposit.setManaged(true);
        paneWithdraw.setManaged(false);
    }
    @FXML
    public void handleEnterWithDraw(){
        menuBar.setVisible(false);
        paneTransactions.setVisible(false);
        paneDeposit.setVisible(false);
        paneWithdraw.setVisible(true);
        menuBar.setManaged(false);
        paneTransactions.setManaged(false);
        paneDeposit.setManaged(false);
        paneWithdraw.setManaged(true);
    }
    @FXML
    public void handleDeposit(){
        if(depAmount.getText()==null || depAmount.getText().isEmpty()){
            ControllerManager.getDashboardController().showToast("Amount is null", null, false);
        }
        else{
            double amount = Double.parseDouble(depAmount.getText().trim());
            DepositWithdrawRequestDTO depositWithdrawRequestDTO = new DepositWithdrawRequestDTO(amount);
            Request request = new Request(ActionType.DEPOSIT, depositWithdrawRequestDTO);
            SocketManager.getClient().sendRequestAsync(request)
                    .thenAccept(response -> {
                        ResponseStatus status = response.getStatus();
                        switch (status){
                            case SUCCESS -> {
                                Platform.runLater(()->{
                                    addBalance(amount);
                                    ControllerManager.getDashboardController().showToast("Deposit succesfully", null, true);
                                    ControllerManager.getDashboardController().updateBalance(UserSession.getBalance(), amount, true);
                                });
                            }
                            case FAILED -> {
                                Platform.runLater(()->{
                                    ControllerManager.getDashboardController().showToast("Deposit Failed", null, false);
                                });
                            }
                        }
                    });

        }
    }
    @FXML
    public void handleWithDraw(){
        if(witAmount.getText()==null || witAmount.getText().isEmpty()){
            ControllerManager.getDashboardController().showToast("Amount is null", null, false);
        }
        else{
            double amount = Double.parseDouble(witAmount.getText().trim());
            DepositWithdrawRequestDTO withdrawRequestDTO = new DepositWithdrawRequestDTO(amount);
            Request request = new Request(ActionType.WITHDRAW, withdrawRequestDTO);
            SocketManager.getClient().sendRequestAsync(request)
                    .thenAccept(response -> {
                        ResponseStatus status = response.getStatus();
                        switch (status){
                            case SUCCESS -> {
                                Platform.runLater(()->{
                                    minusBalance(amount);
                                    ControllerManager.getDashboardController().showToast("Withdraw succesfully", null, true);
                                    ControllerManager.getDashboardController().updateBalance(UserSession.getBalance(), amount, false);
                                });
                            }
                            case FAILED -> {
                                Platform.runLater(()->{
                                    ControllerManager.getDashboardController().showToast("Withdraw Failed", null, false);
                                });
                            }
                        }
                    });
        }
    }
    @Override
    public void initialize(URL location, ResourceBundle resources){
        setAccountViewController(this);
        setUpAccountView();
    }
}
