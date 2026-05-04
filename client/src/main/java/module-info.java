module vn.io.huangnosimp.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires transitive vn.io.huangnosimp.common;
    requires com.google.gson;
    requires java.desktop; // Used for AWT or related libs if necessary

    exports vn.io.huangnosimp;
    exports vn.io.huangnosimp.controller;
    exports vn.io.huangnosimp.Manager;
    exports vn.io.huangnosimp.network;

    opens vn.io.huangnosimp.controller to javafx.fxml, com.google.gson;
    opens vn.io.huangnosimp.model to com.google.gson;
    opens vn.io.huangnosimp to javafx.fxml, javafx.graphics;
    opens vn.io.huangnosimp.Manager to javafx.fxml, com.google.gson;
    opens vn.io.huangnosimp.network to com.google.gson;
}
