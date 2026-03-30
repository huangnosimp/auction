package vn.io.huangnosimp.network;

import vn.io.huangnosimp.model.Message;
import java.io.*;
import java.net.Socket;

public class ClientHandle implements Runnable {
    private  Socket clientSocket;

    public ClientHandle(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("[ClientHandle] Received: " + inputLine);
                Message request = Message.fromJson(inputLine);
                Message response;
                if ("PING".equals(request.getAction())) {
                    response = new Message("PONG", "Server OK!");
                } else {
                    response = new Message("ERROR", "Không hiểu lệnh này");
                }
                out.println(response.toJson());
            }
            } catch (IOException e) {
            System.out.println("[ClientHandle] Client disconnected: " + e.getMessage());
        }

    }
}
