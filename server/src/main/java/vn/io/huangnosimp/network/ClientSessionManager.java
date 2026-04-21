package vn.io.huangnosimp.network;

import vn.io.huangnosimp.protocol.Request;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class ClientSessionManager {
    private static final ClientSessionManager instance = new ClientSessionManager();
    private final Set<ClientHandle> activeClients = new CopyOnWriteArraySet<>();
    private final Map<String, Set<ClientHandle>> auctionRooms = new ConcurrentHashMap<>();

    private ClientSessionManager() {
    }

    public static ClientSessionManager getInstance() {
        return instance;
    }

    public void addClient(ClientHandle client) {
        if (client != null) {
            activeClients.add(client);
        }
    }

    public void removeClient(ClientHandle client) {
        if (client != null) {
            activeClients.remove(client);
            auctionRooms.values().removeIf(room -> {
                room.remove(client);
                return room.isEmpty();
            });
        }
    }

    public void joinRoom(String auctionId, ClientHandle client) {
        if (auctionId != null && client != null) {
            auctionRooms.computeIfAbsent(auctionId, k -> new CopyOnWriteArraySet<>()).add(client);
        }
    }

    public void leaveRoom(String auctionId, ClientHandle client) {
        if (auctionId != null && client != null) {
            auctionRooms.computeIfPresent(auctionId, (key, room) -> {
                room.remove(client);
                return room.isEmpty() ? null : room;
            });
        }
    }

    public void broadcastToRoom(String auctionId, Request request) {
        Set<ClientHandle> room = auctionRooms.get(auctionId);
        if (room != null) {
            for (ClientHandle client : room) {
                try {
                    client.sendRequest(request);
                } catch (Exception e) {
                    System.err.println("[ClientSessionManager] Error broadcasting to room: " + e.getMessage());
                }
            }
        }
    }
}
