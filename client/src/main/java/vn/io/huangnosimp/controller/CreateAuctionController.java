package vn.io.huangnosimp.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.event.ActionEvent;
import vn.io.huangnosimp.Manager.Controllable;
import vn.io.huangnosimp.Manager.ViewManager;

import java.net.URL;
import java.util.ResourceBundle;

public class CreateAuctionController implements Initializable, Controllable {
    private DashboardController parent;
    public void setDashboardController(DashboardController controller){
        this.parent = controller;
    }
    @FXML
    public void handleCancelButton(ActionEvent event){
        ViewManager.changeView("dashboard_home.fxml", 1);
    }
    public void initialize(URL location, ResourceBundle resources){

    }
}
