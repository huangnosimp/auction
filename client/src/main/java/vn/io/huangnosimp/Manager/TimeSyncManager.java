package vn.io.huangnosimp.Manager;

import vn.io.huangnosimp.dto.response.ServerTimeResponseDTO;
import vn.io.huangnosimp.network.SocketClient;
import vn.io.huangnosimp.protocol.ActionType;
import vn.io.huangnosimp.protocol.Request;
import vn.io.huangnosimp.protocol.ResponseStatus;
import vn.io.huangnosimp.util.GsonParser;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public final class TimeSyncManager {
    private static final long SYNC_INTERVAL_SECONDS = 60;
    private static final AtomicLong offsetMillis = new AtomicLong(0);
    private static final AtomicBoolean synced = new AtomicBoolean(false);
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "time-sync");
        thread.setDaemon(true);
        return thread;
    });
    private static ScheduledFuture<?> periodicSync;

    private TimeSyncManager() {
    }

    public static long nowMillis() {
        return System.currentTimeMillis() + (synced.get() ? offsetMillis.get() : 0);
    }

    public static CompletableFuture<Void> syncAsync(SocketClient client) {
        if (client == null || !client.isConnected()) {
            return CompletableFuture.completedFuture(null);
        }

        long sentAt = System.currentTimeMillis();
        return client.sendRequestAsync(new Request(ActionType.GET_SERVER_TIME, null))
                .thenAccept(response -> {
                    long receivedAt = System.currentTimeMillis();
                    if (!ResponseStatus.SUCCESS.equals(response.getStatus()) || response.getData() == null) {
                        return;
                    }
                    ServerTimeResponseDTO dto = GsonParser.GSON.fromJson(
                            GsonParser.GSON.toJsonTree(response.getData()),
                            ServerTimeResponseDTO.class
                    );
                    if (dto != null) {
                        updateOffset(sentAt, dto.getServerTimeMillis(), receivedAt);
                    }
                });
    }

    public static synchronized void startPeriodicSync(SocketClient client) {
        stopPeriodicSync();
        syncAsync(client).exceptionally(ex -> null);
        periodicSync = scheduler.scheduleAtFixedRate(
                () -> syncAsync(client).exceptionally(ex -> null),
                SYNC_INTERVAL_SECONDS,
                SYNC_INTERVAL_SECONDS,
                TimeUnit.SECONDS
        );
    }

    public static synchronized void stopPeriodicSync() {
        if (periodicSync != null) {
            periodicSync.cancel(false);
            periodicSync = null;
        }
    }

    static long calculateOffsetMillis(long clientSentAt, long serverTimeMillis, long clientReceivedAt) {
        return serverTimeMillis - ((clientSentAt + clientReceivedAt) / 2);
    }

    private static void updateOffset(long clientSentAt, long serverTimeMillis, long clientReceivedAt) {
        offsetMillis.set(calculateOffsetMillis(clientSentAt, serverTimeMillis, clientReceivedAt));
        synced.set(true);
    }
}
