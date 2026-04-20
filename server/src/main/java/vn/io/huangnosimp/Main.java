package vn.io.huangnosimp;

import vn.io.huangnosimp.model.SocketServer;
public class Main {
    static void main(String[] args) {
        SocketServer server = new SocketServer(12345);
        server.start();
    }
}
