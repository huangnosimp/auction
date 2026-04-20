package vn.io.huangnosimp.model;

import vn.io.huangnosimp.controller.MessageRouter;

import java.io.*;
import java.net.Socket;

public class ClientHandle implements Runnable {
    private  Socket clientSocket;
    private MessageRouter router;

    public ClientHandle(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.router = new MessageRouter();
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("[ClientHandle] Received: " + inputLine);
                Message request = Message.fromJson(inputLine);
                Message response = router.route(request);
                out.println(response.toJson());
            }
            } catch (IOException e) {
            System.out.println("[ClientHandle] Client disconnected: " + e.getMessage());
        }

    }
}
