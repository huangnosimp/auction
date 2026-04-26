package vn.io.huangnosimp.Manager;
import javafx.animation.*;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.time.Instant;

public class AuctionCountdownUtil {
    private Timeline timeline;
    private Label timeLabel;
    private long remaining;
    private long startTime; // Thêm startTime

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

        // Chưa tới giờ bắt đầu
        if (now < startTime) {
            long waitSeconds = startTime - now;
            long hours   = waitSeconds / 3600;
            long minutes = (waitSeconds % 3600) / 60;
            long seconds = waitSeconds % 60;
            timeLabel.setText(String.format("Bắt đầu sau: %02d:%02d:%02d", hours, minutes, seconds));
            return;
        }

        // Đã kết thúc
        if (remaining <= 0) {
            timeLabel.setText("Đã kết thúc!");
            timeline.stop();
            return;
        }

        // Đang diễn ra
        long hours   = remaining / 3600;
        long minutes = (remaining % 3600) / 60;
        long seconds = remaining % 60;
        timeLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
        remaining--;
    }

    public void start() { timeline.play(); }
    public void stop()  { timeline.stop(); }
}