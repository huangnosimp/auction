package vn.io.huangnosimp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import vn.io.huangnosimp.Manager.FormatUtil;
import vn.io.huangnosimp.dto.response.BidHistoryDTO;

public class BidHistoryCellController {

    @FXML private HBox  cellRoot;
    @FXML private Label avatarLabel;
    @FXML private Label bidderLabel;
    @FXML private Label timeLabel;
    @FXML private Label amountLabel;

    public void setData(BidHistoryDTO dto, boolean isTop) {
        // Avatar: lấy 2 ký tự cuối của tên (ví dụ "Khách #007" → "07")
        String name = dto.getBidder();
        avatarLabel.setText(name.length() >= 2 ? name.substring(name.length() - 2) : name);

        bidderLabel.setText(name);
        timeLabel.setText(String.valueOf(dto.getTimestamp()));
        amountLabel.setText(FormatUtil.formatNumber(dto.getPrice()) + " đ");

        if (isTop) {
            cellRoot.getStyleClass().add("bid-cell-top");
        }
    }
}
