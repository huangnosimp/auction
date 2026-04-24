package vn.io.huangnosimp.Manager;

import javafx.stage.Stage;

public class SceneManager {
    private static Stage stage;
    public static void setStage(Stage st){
        stage = st;
    }
    public static Stage getStage(){
        return stage;
    }
}
