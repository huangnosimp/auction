package vn.io.huangnosimp.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import javafx.event.ActionEvent;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

import static vn.io.huangnosimp.Manager.ViewManager.*;


public class DashboardController implements Initializable {
    @FXML private BorderPane mainBorderPane;
    @FXML private StackPane contentArea; // Cái này nằm ở file dashboard.fxml nên giữ lại
    @FXML private Button createButton;
    @FXML private Button btnOpenSlots;
    @FXML private HBox menuHbox;
    @FXML private VBox sideVbox;

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
    public void initialize(URL location, ResourceBundle resources) {
        setMainBorderPane(mainBorderPane);
        setMainController(this);
        changeView("dashboard_home.fxml", 1);
    }


}
