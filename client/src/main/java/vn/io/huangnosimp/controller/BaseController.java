package vn.io.huangnosimp.controller;

import javafx.application.Platform;
import javafx.fxml.Initializable;
import vn.io.huangnosimp.Manager.SocketManager;
import vn.io.huangnosimp.network.IServerMessageListener;
import vn.io.huangnosimp.protocol.Request;
import vn.io.huangnosimp.protocol.Response;

import java.net.URL;
import java.util.ResourceBundle;

public abstract class BaseController implements Initializable {

    private final IServerMessageListener serverListener = new IServerMessageListener() {
        @Override
        public void onRequestReceived(Request request) {
            Platform.runLater(() -> handleServerNotification(request));
        }

        @Override
        public void onResponseReceived(Response response) {
            Platform.runLater(() -> handleServerResponse(response));
        }

        @Override
        public void onDisconnected(String reason) {
            Platform.runLater(() -> handleDisconnected(reason));
        }
    };

    @Override
    public final void initialize(URL location, ResourceBundle resources) {
        SocketManager.getClient().addListener(serverListener);
        onInit(location, resources);
    }

    public void onExit() {
        SocketManager.getClient().removeListener(serverListener);
    }

    protected abstract void onInit(URL location, ResourceBundle resources);

    protected void handleServerNotification(Request request) {}

    protected void handleServerResponse(Response response) {}

    protected void handleDisconnected(String reason) {
        System.err.println("[BaseController] Mất kết nối: " + reason);
    }
}