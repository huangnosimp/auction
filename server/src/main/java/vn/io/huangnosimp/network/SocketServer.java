package vn.io.huangnosimp.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer {
    private int port;
    public SocketServer(int port) {
        this.port = port;
    }
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[SocketServer] Server started on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[SocketServer] Client connected: " + clientSocket.getInetAddress());
                new Thread(new ClientHandle(clientSocket)).start();
            }

        } catch (IOException e) {
            System.out.println("[SocketServer] Error: " + e.getMessage());
        }
    }
}
