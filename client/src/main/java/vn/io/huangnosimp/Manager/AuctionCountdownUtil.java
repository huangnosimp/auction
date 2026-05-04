package vn.io.huangnosimp.Manager;
import javafx.animation.*;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class AuctionCountdownUtil {
    private Timeline timeline;
    private Label timeLabel;
    private long remaining;
    private long startTime;

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter DEFAULT_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm");

    public AuctionCountdownUtil(Label label, long startTime, long endTime) {
        this.timeLabel = label;
        this.startTime = startTime;
        this.remaining = endTime - Instant.now().getEpochSecond();

        timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> update())
        );
        timeline.setCycleCount(Timeline.INDEFINITE);

        update();
    }

    private void update() {
        long now = Instant.now().getEpochSecond();

        if (now < startTime) {
            long waitSeconds = startTime - now;
            long hours   = waitSeconds / 3600;
            long minutes = (waitSeconds % 3600) / 60;
            long seconds = waitSeconds % 60;
            timeLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
            return;
        }

        if (remaining <= 0) {
            timeLabel.setText("Đã kết thúc!");
            timeline.stop();
            return;
        }

        long hours   = remaining / 3600;
        long minutes = (remaining % 3600) / 60;
        long seconds = remaining % 60;
        timeLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
        remaining--;
    }

    // ─── Tiện ích chuyển đổi ───────────────────────────────────────────────

    /** Chuyển epoch seconds → "dd/MM/yyyy · HH:mm" */
    public static String formatEpochSecond(long epochSecond) {
        return LocalDateTime
                .ofInstant(Instant.ofEpochSecond(epochSecond), VN_ZONE)
                .format(DEFAULT_FORMAT);
    }

    /** Chuyển epoch seconds với pattern tuỳ chỉnh */
    public static String formatEpochSecond(long epochSecond, String pattern) {
        return LocalDateTime
                .ofInstant(Instant.ofEpochSecond(epochSecond), VN_ZONE)
                .format(DateTimeFormatter.ofPattern(pattern));
    }

    // ──────────────────────────────────────────────────────────────────────

    public void start() { timeline.play(); }
    public void stop()  { timeline.stop(); }
}