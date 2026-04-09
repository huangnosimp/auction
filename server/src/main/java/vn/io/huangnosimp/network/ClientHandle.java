package vn.io.huangnosimp.network;

import vn.io.huangnosimp.controller.MessageRouter;

import java.io.*;
import java.net.Socket;
import com.google.gson.JsonSyntaxException;
import vn.io.huangnosimp.model.UserType;

public class ClientHandle implements Runnable {
    private  final Socket clientSocket;
    private final MessageRouter router;
    private String userId;
    private UserType userType;

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

    @Override
    public void run() {
        PrintWriter out = null;
        try (Socket socket = this.clientSocket;
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter tryOut = new PrintWriter(socket.getOutputStream(), true)) {
             
            out = tryOut;
            ClientSessionManager.getInstance().addClient(out);

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("[ClientHandle] Received: " + inputLine);

                try {
                    Request request = Request.fromJson(inputLine);
                    Response response = router.route(request, this);

                    if (response != null) {
                        out.println(response.toJson());
                    }
                } catch (JsonSyntaxException e) {
                    System.out.println("[ClientHandle] Invalid JSON format from client.");
                    out.println("{\"error\": \"Invalid JSON\"}");
                } catch (Exception e) {
                    System.out.println("[ClientHandle] Error processing message: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("[ClientHandle] Client disconnected or I/O error: " + e.getMessage());
        } finally {
            if (out != null) {
                ClientSessionManager.getInstance().removeClient(out);
            }
        }

    }
}
