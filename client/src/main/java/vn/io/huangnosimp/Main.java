package vn.io.huangnosimp;

import vn.io.huangnosimp.network.SocketClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        final int port = 26676;
        String host = "127.0.0.1";

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("./client/config.properties")) {
            props.load(fis);
            host = props.getProperty("SERVER_IP", host);
            System.out.println("Loaded config: " + host + ":" + port);
        } catch (IOException e) {
            System.out.println("Could not load config.properties, using default host");
        }
        SocketClient client = new SocketClient(host, port);
        try {
            client.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
