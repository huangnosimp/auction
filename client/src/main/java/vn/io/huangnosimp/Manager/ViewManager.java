package vn.io.huangnosimp.Manager;

import vn.io.huangnosimp.controller.DashboardController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ViewManager {
    private static final Map<String, Parent> cache = new HashMap<>();

    private static BorderPane mainBorderPane;

    private static DashboardController mainController;

    public static void setMainBorderPane(BorderPane area){
        mainBorderPane = area;
    }

    public static void setMainController(DashboardController controller) {
        mainController = controller;
    }

    public static void changeView(String fxmlFile, int mode){
        try {

            Parent root;
            if (mode == 1 && cache.containsKey(fxmlFile)) {
                root = cache.get(fxmlFile);
            }
            else {
                FXMLLoader loader = new FXMLLoader(ViewManager.class.getResource("/fxml/" + fxmlFile));
                root = loader.load();
                Object controller = loader.getController();
                if(controller instanceof Controllable){
                    ((Controllable) controller).setDashboardController(mainController);
                }
                if(mode == 1){
                    cache.put(fxmlFile, root);
                }
            }
            mainBorderPane.setCenter(root);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
