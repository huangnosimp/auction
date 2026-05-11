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
    private long startTime;
    private long endTime;

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter DEFAULT_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm");

    public AuctionCountdownUtil(Label label, long startTime, long endTime) {
        this.timeLabel = label;
        this.startTime = startTime;
        this.endTime = endTime;

        timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> update())
        );
        timeline.setCycleCount(Timeline.INDEFINITE);

        update();
    }

    private void update() {
        long now = Instant.now().toEpochMilli();

        if (now < startTime) {
            long diffMilli = startTime - now;
            displayTime(diffMilli);
            return;
        }

        long remainingMilli = endTime - now;

        if (remainingMilli <= 0) {
            timeLabel.setText("Đã kết thúc!");
            timeline.stop();
            return;
        }

        displayTime(remainingMilli);
    }

    private void displayTime(long diffMilli) {

        long totalSeconds = diffMilli / 1000;

        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        timeLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
    }

    public static String formatEpochSecond(long epochSecond) {//chỉnh hiển thị theo form
        return LocalDateTime
                .ofInstant(Instant.ofEpochMilli(epochSecond), VN_ZONE)
                .format(DEFAULT_FORMAT);
    }

    public static String formatEpochSecond(long epochSecond, String pattern) {//chỉnh theo form tùy chỉnh
        return LocalDateTime
                .ofInstant(Instant.ofEpochMilli(epochSecond), VN_ZONE)
                .format(DateTimeFormatter.ofPattern(pattern));
    }

    public void start() {
        timeline.play();
    }

    public void stop() {
        timeline.stop();
    }
}