package vn.io.huangnosimp.network;

import vn.io.huangnosimp.controller.MessageRouter;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketServer {
    private static final Logger logger = LoggerFactory.getLogger(SocketServer.class);
    private static final int THREAD_NUMBER = 50;
    private final int port;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_NUMBER);
    private final MessageRouter messageRouter;
    public SocketServer(int port, MessageRouter messageRouter) {
        this.port = port;
        this.messageRouter = messageRouter;
    }
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Server started port={}", port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                logger.info("Client accepted remoteAddress={}", clientSocket.getInetAddress());
                threadPool.execute(new ClientHandle(clientSocket, messageRouter));
            }

        } catch (IOException e) {
            logger.error("Socket server error port={}", port, e);
        } finally {
            threadPool.shutdown();
        }
    }
}
