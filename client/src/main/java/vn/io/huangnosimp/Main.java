package vn.io.huangnosimp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import vn.io.huangnosimp.Manager.SceneManager;
import vn.io.huangnosimp.Manager.SocketManager;
import vn.io.huangnosimp.Manager.TimeSyncManager;
import vn.io.huangnosimp.network.SocketClient;

import java.lang.reflect.Method;
import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            SceneManager.setStage(primaryStage);
            URL fxmlLocation = getClass().getResource("/fxml/login.fxml");
            if (fxmlLocation == null) {
                throw new Exception("Không tìm thấy file login.fxml!");
            }

            Parent root = FXMLLoader.load(fxmlLocation);
            primaryStage.setScene(new Scene(root, 1000, 800));
            primaryStage.setTitle("Dashboard");
            primaryStage.show();

            new Thread(() -> {
                try {
                    SocketClient socketClient = new SocketClient("20.255.57.143", 26676);
                    socketClient.connect();
                    SocketManager.setClient(socketClient);
                    TimeSyncManager.startPeriodicSync(socketClient);
                    System.out.println("Kết nối server thành công!");
                } catch (Exception e) {
                    System.err.println("Lỗi kết nối Server: " + e.getMessage());
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
