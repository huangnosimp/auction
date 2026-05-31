package vn.io.huangnosimp.Manager;

import javafx.scene.Scene;
import vn.io.huangnosimp.controller.DashboardController;
import vn.io.huangnosimp.controller.liveAuctionController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ViewManager {
    private static final Map<String, Parent> cache = new HashMap<>();

    private static BorderPane mainBorderPane;

    public static void setMainBorderPane(BorderPane area){
        mainBorderPane = area;
    }

    public static Map<String, Parent> getCache(){
        return cache;
    }

    public static void changeView(String fxmlFile, int mode){
        try {
            if (ControllerManager.getLiveAuctionController() != null) {
                ControllerManager.getLiveAuctionController().cleanup();
            }

            Parent root;
            if (mode == 1 && cache.containsKey(fxmlFile)) {
                root = cache.get(fxmlFile);
            }
            else {
                FXMLLoader loader = new FXMLLoader(ViewManager.class.getResource("/fxml/" + fxmlFile));
                root = loader.load();

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

    public static <T> T changeViewWithController(String fxmlFile){
        try{
            if (ControllerManager.getLiveAuctionController() != null) {
                ControllerManager.getLiveAuctionController().cleanup();
            }

            FXMLLoader loader = new FXMLLoader(ViewManager.class.getResource("/fxml/" + fxmlFile));
            Parent root = loader.load();
            mainBorderPane.setCenter(root);
            return loader.getController();
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }
    public static void changeMainStage(String fxmlFile){
        try{
            if (ControllerManager.getLiveAuctionController() != null) {
                ControllerManager.getLiveAuctionController().cleanup();
            }

            javafx.stage.Stage stage = SceneManager.getStage();
            double width = stage.getWidth();
            double height = stage.getHeight();
            boolean isMaximized = stage.isMaximized();

            FXMLLoader loader = new FXMLLoader(ViewManager.class.getResource("/fxml/" + fxmlFile));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);

            if (!isMaximized) {
                stage.setWidth(width);
                stage.setHeight(height);
            } else {
                stage.setMaximized(true);
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
