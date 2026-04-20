package vn.io.huangnosimp.network;

import vn.io.huangnosimp.controller.MessageRouter;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketServer {
    private final int port;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(50);
    private final MessageRouter messageRouter;
    public SocketServer(int port, MessageRouter messageRouter) {
        this.port = port;
        this.messageRouter = messageRouter;
    }
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[SocketServer] Server started on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[SocketServer] Client connected: " + clientSocket.getInetAddress());
                threadPool.execute(new ClientHandle(clientSocket, messageRouter));
            }

        } catch (IOException e) {
            System.out.println("[SocketServer] Error: " + e.getMessage());
        } finally {
            threadPool.shutdown();
        }
    }
}
