package vn.io.huangnosimp.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketServer {
    private int port;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(100);
    public SocketServer(int port) {
        this.port = port;
    }
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[SocketServer] Server started on port " + port);
            
            // Khởi tạo router dùng chung cho tất cả các kết nối, tránh tạo hàng loạt Controller mới cho mỗi request
            vn.io.huangnosimp.controller.MessageRouter sharedRouter = new vn.io.huangnosimp.controller.MessageRouter();
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[SocketServer] Client connected: " + clientSocket.getInetAddress());
                threadPool.execute(new ClientHandle(clientSocket, sharedRouter));
            }

        } catch (IOException e) {
            System.out.println("[SocketServer] Error: " + e.getMessage());
        } finally {
            threadPool.shutdown();
        }
    }
}
