package vn.io.huangnosimp.Manager;

import vn.io.huangnosimp.network.SocketClient;

public class SocketManager {
    private static SocketClient socketClient;

    public static SocketClient getClient() { return socketClient; }
    public static void setClient(SocketClient sc) { socketClient = sc; }
}