package vn.io.huangnosimp.network;

import java.io.PrintWriter;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class ClientSessionManager {
    private static final ClientSessionManager instance = new ClientSessionManager();
    private final Set<PrintWriter> activeClients = new CopyOnWriteArraySet<>();

    private ClientSessionManager() {
    }

    public static ClientSessionManager getInstance() {
        return instance;
    }

    public void addClient(PrintWriter out) {
        if (out != null) {
            activeClients.add(out);
        }
    }

    public void removeClient(PrintWriter out) {
        if (out != null) {
            activeClients.remove(out);
        }
    }

    public void broadcast(Message message) {
        String jsonStr = message.toJson();
        System.out.println("[ClientSessionManager] Broadcasting: " + jsonStr);
        for (PrintWriter out : activeClients) {
            try {
                out.println(jsonStr);
            } catch (Exception e) {
                System.err.println("[ClientSessionManager] Lỗi khi gửi tin nhắn cho 1 client: " + e.getMessage());
            }
        }
    }
}
