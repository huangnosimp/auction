package vn.io.huangnosimp.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import vn.io.huangnosimp.Manager.UserSession;
import vn.io.huangnosimp.Manager.ViewManager;

import java.net.URL;
import java.util.ResourceBundle;

import static vn.io.huangnosimp.Manager.ControllerManager.clearAllController;
import static vn.io.huangnosimp.Manager.ControllerManager.setAccountViewController;
import static vn.io.huangnosimp.Manager.UserSession.clearAllSession;

public class AccountViewController implements Initializable {
    @FXML private HBox menuBar;
    @FXML private VBox paneTransactions;
    @FXML private VBox paneDeposit;
    @FXML private VBox paneWithdraw;
    @FXML private Button btnDeposit;
    @FXML private Button btnWithdraw;

    @FXML private Label usernameLabel;

    private void setUpAccountView(){
        usernameLabel.setText(UserSession.getUsername());
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
    public void handleDeposit(){
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
    public void handleWithDraw(){
        menuBar.setVisible(false);
        paneTransactions.setVisible(false);
        paneDeposit.setVisible(false);
        paneWithdraw.setVisible(true);
        menuBar.setManaged(false);
        paneTransactions.setManaged(false);
        paneDeposit.setManaged(false);
        paneWithdraw.setManaged(true);
    }
    @Override
    public void initialize(URL location, ResourceBundle resources){
        setAccountViewController(this);
        setUpAccountView();
    }
}
