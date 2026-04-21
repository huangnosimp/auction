package vn.io.huangnosimp.network;

import com.google.gson.Gson;
import javafx.application.Platform; // giữ nếu bạn dùng Platform.runLater
import vn.io.huangnosimp.protocol.Request;
import vn.io.huangnosimp.protocol.Response;

import java.io.*;
import java.net.Socket;

public class SocketClient {
    private static SocketClient instance;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final Gson gson = new Gson();
    private boolean isRunning = true;

    private SocketClient(String host, int port){
        try {
            this.socket = new Socket(host, port);
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        catch (IOException e){
            System.out.println("[SocketClient] Error: " + e.getMessage());
        }
    }
    public void sendRequest(Request request){
        if(out != null){
            String json = gson.toJson(request);
            out.println(json);
        }
    }
}
