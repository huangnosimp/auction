package vn.io.huangnosimp.network;

import vn.io.huangnosimp.controller.MessageRouter;
import vn.io.huangnosimp.model.Message;
import java.io.*;
import java.net.Socket;
import com.google.gson.JsonSyntaxException;

public class ClientHandle implements Runnable {
    private  final Socket clientSocket;
    private final MessageRouter router;

    public ClientHandle(Socket clientSocket, MessageRouter router) {
        this.clientSocket = clientSocket;
        this.router = router;
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
                    Message request = Message.fromJson(inputLine);
                    Message response = router.route(request);

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
