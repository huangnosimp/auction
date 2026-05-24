package vn.io.huangnosimp.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import vn.io.huangnosimp.Manager.ControllerManager;
import vn.io.huangnosimp.Manager.SocketManager;
import vn.io.huangnosimp.Manager.UserSession;
import vn.io.huangnosimp.dto.request.AutoBidRequestDTO;
import vn.io.huangnosimp.dto.response.AutoBidResponseDTO;
import vn.io.huangnosimp.protocol.ActionType;
import vn.io.huangnosimp.protocol.Request;
import vn.io.huangnosimp.protocol.ResponseStatus;

import static vn.io.huangnosimp.Manager.FormatUtil.formatNumber;

public class AutoBidItemController {
    @FXML private Label lblProductTitle;
    @FXML private Label lblMaxBid;
    @FXML private Button btnCancel;

    private String auctionId;

    public void setUp(AutoBidResponseDTO dto, String productName) {
        this.auctionId = dto.getAuctionId();
        lblProductTitle.setText(productName != null ? productName : "Auction #" + auctionId);
        lblMaxBid.setText("Max: " + formatNumber(dto.getMaxBid()) + " VND");
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        if (auctionId == null) return;
        btnCancel.setDisable(true);

        AutoBidRequestDTO requestDTO = new AutoBidRequestDTO(auctionId, 0, 0);
        Request request = new Request(ActionType.UNREGISTER_AUTO_BID, requestDTO);

        SocketManager.getClient().sendRequestAsync(request)
            .thenAccept(response -> {
                Platform.runLater(() -> {
                    if (ResponseStatus.SUCCESS.equals(response.getStatus())) {
                        UserSession.removeAutoBid(auctionId);
                        ControllerManager.getDashboardController().removeAutoBidItem(auctionId);
                        ControllerManager.getDashboardController().showToast("Auto Bid", "Cancelled for this auction", true);
                    } else {
                        ControllerManager.getDashboardController().showToast("FAILED", response.getMessage(), false);
                        btnCancel.setDisable(false);
                    }
                });
            });
    }
}
