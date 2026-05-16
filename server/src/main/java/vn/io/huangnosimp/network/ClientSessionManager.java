package vn.io.huangnosimp.network;

import vn.io.huangnosimp.protocol.ActionType;
import vn.io.huangnosimp.protocol.Request;
import vn.io.huangnosimp.protocol.Response;
import vn.io.huangnosimp.protocol.ResponseStatus;

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

    public boolean isUserInRoom(String auctionId, String userId) {
        if (auctionId == null || userId == null) return false;
        Set<ClientHandle> room = auctionRooms.get(auctionId);
        if (room != null) {
            for (ClientHandle client : room) {
                if (userId.equals(client.getUserId())) {
                    return true;
                }
            }
        }
        return false;
    }

    public void sendToUser(String userId, Request request) {
        if (userId == null) return;
        for (ClientHandle client : activeClients) {
            if (userId.equals(client.getUserId())) {
                try {
                    client.sendRequest(request);
                } catch (Exception e) {
                    System.err.println("[ClientSessionManager] Error sending to user " + userId + ": " + e.getMessage());
                }
            }
        }
    }

    public void banUser(String userId) {
        if (userId == null) return;

        for (ClientHandle client : activeClients) {
            if (userId.equals(client.getUserId())) {
                try {
                    client.sendResponse(new Response(ResponseStatus.BANNED, "You have been banned by the administrator."));
                    client.close();
                    removeClient(client);
                    System.out.println("[ClientSessionManager] Banned and disconnected user: " + userId);
                    break;
                } catch (Exception e) {
                    System.err.println("[ClientSessionManager] Error while banning user " + userId + ": " + e.getMessage());
                }
            }
        }
    }

    public void destroyRoom(String auctionId) {
        if (auctionId == null) return;

        Request cancelMsg = new Request(
                ActionType.ADMIN_FORCE_CANCEL,
                "This auction is destroyed by Admin."
        );
        broadcastToRoom(auctionId, cancelMsg);

        auctionRooms.remove(auctionId);
        System.out.println("[ClientSessionManager] Destroyed room for canceled auction: " + auctionId);
    }

    public boolean isUserOnline(String userId) {
        if (userId == null) return false;
        for (ClientHandle client : activeClients) {
            if (userId.equals(client.getUserId())) {
                return true;
            }
        }
        return false;
    }
}
