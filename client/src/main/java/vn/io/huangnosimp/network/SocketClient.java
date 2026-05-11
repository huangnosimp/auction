package vn.io.huangnosimp.network;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import vn.io.huangnosimp.protocol.ActionType;
import vn.io.huangnosimp.protocol.Request;
import vn.io.huangnosimp.protocol.Response;
import vn.io.huangnosimp.util.GsonParser;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class SocketClient {
    private final String host;
    private final int port;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Thread listenerThread;

    private final List<IServerMessageListener> listeners = new CopyOnWriteArrayList<>();

    // Lưu future + ActionType enum trong cùng 1 entry
    private record PendingEntry(CompletableFuture<Response> future, ActionType action) {}
    private final ConcurrentHashMap<String, PendingEntry> pendingRequests = new ConcurrentHashMap<>();

    public SocketClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void addListener(IServerMessageListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(IServerMessageListener listener) {
        listeners.remove(listener);
    }

    public void connect() throws IOException {
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        System.out.println("[SocketClient] Connected to server at " + host + ":" + port);

        listenerThread = new Thread(this::listenForMessages);
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public CompletableFuture<Response> sendRequestAsync(Request request) {
        CompletableFuture<Response> future = new CompletableFuture<>();
        if (out != null && socket != null && !socket.isClosed()) {
            pendingRequests.put(
                    request.getRequestId(),
                    new PendingEntry(future, request.getAction()) // ActionType enum
            );
            String jsonStr = GsonParser.GSON.toJson(request);
            synchronized (out) {
                out.println(jsonStr);
            }
            System.out.println("[SocketClient] Sent: " + jsonStr);
        } else {
            future.completeExceptionally(new IOException("Not connected to server."));
        }
        return future;
    }

    public Response sendRequestBlocking(Request request) throws Exception {
        return sendRequestAsync(request).get();
    }

    public void sendRequest(Request request) {
        if (out != null && socket != null && !socket.isClosed()) {
            pendingRequests.put(
                    request.getRequestId(),
                    new PendingEntry(new CompletableFuture<>(), request.getAction())
            );
            String jsonStr = GsonParser.GSON.toJson(request);
            synchronized (out) {
                out.println(jsonStr);
            }
            System.out.println("[SocketClient] Sent (Fire & Forget with Tracking): " + jsonStr);
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
                    JsonObject jsonObject = JsonParser.parseString(inputLine).getAsJsonObject();

                    if (jsonObject.has("status") || jsonObject.has("message")) {
                        Response response = GsonParser.GSON.fromJson(jsonObject, Response.class);
                        handleResponse(response);
                    } else if (jsonObject.has("action")) {
                        Request request = GsonParser.GSON.fromJson(jsonObject, Request.class);
                        handleServerNotification(request);
                    } else {
                        System.out.println("[SocketClient] Unknown message format: " + inputLine);
                    }

                } catch (Exception e) {
                    System.err.println("[SocketClient] Failed to parse JSON from server: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("[SocketClient] Connection closed or lost: " + e.getMessage());
            notifyDisconnect(e.getMessage());
        } finally {
            disconnect();
        }
    }

    private void handleResponse(Response response) {
        if (response.getRequestId() != null) {
            PendingEntry entry = pendingRequests.remove(response.getRequestId());
            if (entry != null) {
                ActionType action = entry.action();
                System.out.println("[SocketClient] Response for action: " + action);

                for (IServerMessageListener listener : listeners) {
                    try {
                        listener.onResponseReceived(response, action);
                    } catch (Exception e) {
                        System.err.println("[SocketClient] Error in Response listener: " + e.getMessage());
                    }
                }

                entry.future().complete(response);
            }
        }
    }

    private void handleServerNotification(Request request) {
        for (IServerMessageListener listener : listeners) {
            try {
                listener.onRequestReceived(request);
            } catch (Exception e) {
                System.err.println("[SocketClient] Error in Notification listener: " + e.getMessage());
            }
        }
    }

    private void notifyDisconnect(String reason) {
        for (PendingEntry entry : pendingRequests.values()) {
            entry.future().completeExceptionally(new IOException("Connection lost: " + reason));
        }
        pendingRequests.clear();

        for (IServerMessageListener listener : listeners) {
            try {
                listener.onDisconnected(reason);
            } catch (Exception e) {
                System.err.println("[SocketClient] Error notifying disconnect: " + e.getMessage());
            }
        }
    }

    public void disconnect() {
        if (listenerThread != null && listenerThread.isAlive()) {
            listenerThread.interrupt();
        }
        try {
            if (in != null) in.close();
        } catch (IOException e) {
            System.err.println("[SocketClient] Error closing input stream: " + e.getMessage());
        }

        if (out != null) out.close();

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("[SocketClient] Error closing socket: " + e.getMessage());
        }
        System.out.println("[SocketClient] Disconnected cleanly.");
    }
}
