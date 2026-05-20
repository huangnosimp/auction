package vn.io.huangnosimp.network;

import vn.io.huangnosimp.util.GsonParser;
import vn.io.huangnosimp.protocol.Request;
import vn.io.huangnosimp.protocol.Response;
import vn.io.huangnosimp.controller.MessageRouter;

import java.io.*;
import java.net.Socket;
import com.google.gson.JsonSyntaxException;
import vn.io.huangnosimp.enums.UserType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientHandle implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandle.class);
    private  final Socket clientSocket;
    private final MessageRouter router;
    private String userId;

    private UserType userType;
    private PrintWriter out;

    public ClientHandle(Socket clientSocket, MessageRouter router) {
        this.clientSocket = clientSocket;
        this.router = router;
        this.userId = null;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public void sendResponse(Response response) {
        if (out != null) {
            out.println(GsonParser.GSON.toJson(response));
        }
    }

    public void sendRequest(Request request) {
        if (out != null) {
            out.println(GsonParser.GSON.toJson(request));
        }
    }

    @Override
    public void run() {
        try (Socket socket = this.clientSocket;
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter tryOut = new PrintWriter(socket.getOutputStream(), true)) {
             
            this.out = tryOut;
            ClientSessionManager.getInstance().addClient(this);
            logger.info("Client connected remoteAddress={}", clientSocket.getInetAddress());

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                try {
                    Request request = GsonParser.GSON.fromJson(inputLine, Request.class);
                    Response response = router.route(request, this);

                    if (response != null) {
                        if (request != null && request.getRequestId() != null) {
                            response.setRequestId(request.getRequestId());
                        }
                        sendResponse(response);
                    }
                } catch (JsonSyntaxException e) {
                    logger.warn("Invalid JSON format from client remoteAddress={}", clientSocket.getInetAddress());
                    this.out.println("{\"error\": \"Invalid JSON\"}");
                } catch (Exception e) {
                    logger.error("Error processing client message remoteAddress={}", clientSocket.getInetAddress(), e);
                }
            }
        } catch (IOException e) {
            logger.info("Client disconnected or I/O error remoteAddress={}", clientSocket.getInetAddress(), e);
        } finally {
            ClientSessionManager.getInstance().removeClient(this);
        }

    }
    public void close() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            logger.warn("Error closing client socket remoteAddress={}", clientSocket.getInetAddress(), e);
        }
    }
}
