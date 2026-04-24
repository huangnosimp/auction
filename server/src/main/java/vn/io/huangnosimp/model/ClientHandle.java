package vn.io.huangnosimp.model;

import vn.io.huangnosimp.network.ClientSessionManager;
import vn.io.huangnosimp.util.GsonParser;
import vn.io.huangnosimp.protocol.Request;
import vn.io.huangnosimp.protocol.Response;
import vn.io.huangnosimp.controller.MessageRouter;

import java.io.*;
import java.net.Socket;
import com.google.gson.JsonSyntaxException;
import vn.io.huangnosimp.enums.UserType;

public class ClientHandle implements Runnable {
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

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("[ClientHandle] Received: " + inputLine);

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
                    System.out.println("[ClientHandle] Invalid JSON format from client.");
                    this.out.println("{\"error\": \"Invalid JSON\"}");
                } catch (Exception e) {
                    System.out.println("[ClientHandle] Error processing message: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("[ClientHandle] Client disconnected or I/O error: " + e.getMessage());
        } finally {
            ClientSessionManager.getInstance().removeClient(this);
        }

    }
    public void close() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("[ClientHandle] Error closing client socket: " + e.getMessage());
        }
    }
}
