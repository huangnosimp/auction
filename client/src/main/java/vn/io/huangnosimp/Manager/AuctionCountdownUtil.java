package vn.io.huangnosimp.Manager;
import javafx.animation.*;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.time.Instant;

public class AuctionCountdownUtil {
    private Timeline timeline;
    private Label timeLabel;
    private long remaining; // Số giây còn lại

    public AuctionCountdownUtil(Label label, long endTime) {
        this.timeLabel = label;
        // Tính remaining 1 lần duy nhất lúc khởi tạo
        this.remaining = endTime - Instant.now().getEpochSecond();

        timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> update())
        );
        timeline.setCycleCount(Timeline.INDEFINITE);

        update();
    }

    private void update() {
        if (remaining <= 0) {
            timeLabel.setText("Đã kết thúc!");
            timeline.stop();
            return;
        }

        long hours   = remaining / 3600;
        long minutes = (remaining % 3600) / 60;
        long seconds = remaining % 60;

        timeLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
        remaining--; // Giảm 1 giây sau mỗi lần cập nhật
    }

    public void start() { timeline.play(); }
    public void stop()  { timeline.stop(); }
}