package vn.io.huangnosimp.network;

import java.io.*;
import java.net.Socket;

public class SocketClient {
    private final String host;
    private final int port;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Thread listenerThread;

    public SocketClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    
    public void connect() throws IOException {
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true); 
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        System.out.println("[SocketClient] Connected to server at " + host + ":" + port);

        
        listenerThread = new Thread(this::listenForMessages);
        listenerThread.start();
    }

    
    public void sendMessage(Message message) {
        if (out != null && socket != null && !socket.isClosed()) {
            String jsonStr = message.toJson();
            out.println(jsonStr);
            System.out.println("[SocketClient] Sent: " + jsonStr);
        } else {
            System.err.println("[SocketClient] Cannot send message, not connected to server.");
        }
    }


    private void listenForMessages() {
        try {
            String inputLine;
            while (!Thread.currentThread().isInterrupted() && (inputLine = in.readLine()) != null) {
                System.out.println("[SocketClient] Raw Received: " + inputLine);
                try {
                    Message response = Message.fromJson(inputLine);
                    handleResponse(response);
                } catch (Exception e) {
                    System.err.println("[SocketClient] Failed to parse JSON from server.");
                }
            }
        } catch (IOException e) {
            System.out.println("[SocketClient] Connection closed or lost: " + e.getMessage());
        } finally {
            disconnect(); 
        }
    }

    
    private void handleResponse(Message response) {
        System.out.println("[SocketClient] Parsed Response -> Action: " + response.getAction() + ", Data: " + response.getData());
    }


    public void disconnect() {
        try {
            if (listenerThread != null && listenerThread.isAlive()) {
                listenerThread.interrupt();
            }
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            System.out.println("[SocketClient] Disconnected cleanly.");
        } catch (IOException e) {
            System.err.println("[SocketClient] Error during disconnect: " + e.getMessage());
        }
    }
}
