package vn.io.huangnosimp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.lang.reflect.Method;
import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Luôn load dashboard.fxml trực tiếp (bỏ qua LoginView)
            URL fxml = getClass().getResource("/fxml/dashboard.fxml");
            if (fxml == null) {
                System.err.println("Không tìm thấy /fxml/dashboard.fxml trên classpath.");
                Platform.exit();
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxml);
            Parent root = loader.load();

            // Nếu controller có phương thức setPrimaryStage(Stage), truyền stage vào (tùy chọn)
            Object controller = loader.getController();
            if (controller != null) {
                try {
                    Method m = controller.getClass().getMethod("setPrimaryStage", Stage.class);
                    if (m != null) {
                        m.invoke(controller, primaryStage);
                    }
                } catch (NoSuchMethodException ignored) {
                    // Không có setPrimaryStage — bình thường, bỏ qua
                } catch (Exception ex) {
                    System.err.println("Không thể gọi setPrimaryStage trên controller: " + ex.getMessage());
                }
            }

            Scene scene = new Scene(root);
            primaryStage.setTitle("Dashboard");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(500);
            primaryStage.show();
        } catch (Exception e) {
            // In stacktrace để bạn thấy chính xác lỗi (ví dụ FXML lỗi, thiếu import, handler thiếu...)
            e.printStackTrace();
            Platform.exit();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
