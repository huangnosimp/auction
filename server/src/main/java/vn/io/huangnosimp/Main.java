package vn.io.huangnosimp;

import vn.io.huangnosimp.network.SocketServer;
import vn.io.huangnosimp.service.AuctionService;

public class Main {
    static void main(String[] args) {
        SocketServer server = new SocketServer(12345);
        server.start();
        AuctionService.getInstance();
    }
}
