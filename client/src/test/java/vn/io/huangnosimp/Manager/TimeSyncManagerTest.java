package vn.io.huangnosimp.Manager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeSyncManagerTest {
    @Test
    void calculateOffsetUsesRoundTripMidpoint() {
        long offset = TimeSyncManager.calculateOffsetMillis(1_000, 1_600, 1_200);

        assertEquals(500, offset);
    }
}
