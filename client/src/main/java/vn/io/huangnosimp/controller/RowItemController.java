package vn.io.huangnosimp.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import vn.io.huangnosimp.Manager.AuctionCountdownUtil;
import vn.io.huangnosimp.Manager.FormatUtil;
import vn.io.huangnosimp.Manager.SocketManager;
import vn.io.huangnosimp.Manager.TimeSyncManager;
import vn.io.huangnosimp.Manager.UserSession;
import vn.io.huangnosimp.Manager.ViewManager;
import vn.io.huangnosimp.Manager.ControllerManager;
import vn.io.huangnosimp.dto.request.JoinRoomRequestDTO;
import vn.io.huangnosimp.dto.request.GetAuctionDetailRequestDTO;
import vn.io.huangnosimp.dto.response.AuctionCardDTO;
import vn.io.huangnosimp.dto.response.AuctionDetailResponseDTO;
import vn.io.huangnosimp.protocol.ActionType;
import vn.io.huangnosimp.protocol.Request;
import vn.io.huangnosimp.protocol.ResponseStatus;
import vn.io.huangnosimp.util.GsonParser;

import java.util.List;

public class RowItemController {
    @FXML private ImageView imgProduct;
    @FXML private Label lblProductName;
    
    // Fields for Won Item Row
    @FXML private Label lblPaidPrice;
    @FXML private Label lblWonAt;

    // Fields for Listing Row
    @FXML private Label lblBidCount;
    @FXML private Label lblTopBid;
    @FXML private Label lblEndsIn;
    @FXML private Label lblEndDate;
    @FXML private Button btnAction;

    private String auctionId;
    private List<String> displayImg;

    public void setUpWonRow(AuctionCardDTO dto) {
        this.auctionId = dto.getAuctionId();
        this.displayImg = dto.getImageUrl();

        if (displayImg != null && !displayImg.isEmpty()) {
            imgProduct.setImage(new Image(displayImg.get(0), 0, 0, true, true));
        } else {
            imgProduct.setImage(null);
        }

        if (lblProductName != null) {
            lblProductName.setText(dto.getProductName());
        }

        if (lblPaidPrice != null) {
            lblPaidPrice.setText(FormatUtil.formatNumber(dto.getYourBid()));
        }

        if (lblWonAt != null) {
            lblWonAt.setText(AuctionCountdownUtil.formatEpochSecond(dto.getEndTime()));
        }
    }

    public void setUpListingRow(AuctionCardDTO dto) {
        this.auctionId = dto.getAuctionId();
        this.displayImg = dto.getImageUrl();

        if (displayImg != null && !displayImg.isEmpty()) {
            imgProduct.setImage(new Image(displayImg.get(0), 0, 0, true, true));
        } else {
            imgProduct.setImage(null);
        }

        if (lblProductName != null) {
            lblProductName.setText(dto.getProductName());
        }

        if (lblBidCount != null) {
            lblBidCount.setText(String.valueOf(dto.getBidCount()));
        }

        if (lblTopBid != null) {
            lblTopBid.setText(FormatUtil.formatNumber(dto.getCurrentPrice()));
        }

        if (lblEndsIn != null) {
            AuctionCountdownUtil countdown = new AuctionCountdownUtil(lblEndsIn, dto.getStartTime(), dto.getEndTime());
            countdown.start();
        }

        if (lblEndDate != null) {
            lblEndDate.setText(AuctionCountdownUtil.formatEpochSecond(dto.getEndTime()));
        }

        if (btnAction != null) {
            boolean active = TimeSyncManager.nowMillis() < dto.getEndTime();
            btnAction.setVisible(active);
            btnAction.setManaged(active);
            btnAction.setOnAction(e -> handleEnterRoomAsManager(dto));
        }
    }

    private void handleEnterRoomAsManager(AuctionCardDTO dto) {
        String targetAuctionId = dto.getAuctionId();
        List<String> images = dto.getImageUrl();

        JoinRoomRequestDTO joinRoomRequest = new JoinRoomRequestDTO(targetAuctionId);
        Request request = new Request(ActionType.JOIN_ROOM, joinRoomRequest);

        SocketManager.getClient().sendRequestAsync(request)
                .thenAccept(joinRoomResponse -> {
                    if (ResponseStatus.SUCCESS.equals(joinRoomResponse.getStatus())) {
                        GetAuctionDetailRequestDTO getDetail = new GetAuctionDetailRequestDTO(targetAuctionId);
                        SocketManager.getClient().sendRequestAsync(new Request(ActionType.GET_AUCTION_DETAIL, getDetail))
                                .thenAccept(response -> {
                                    if (ResponseStatus.SUCCESS.equals(response.getStatus())) {
                                        AuctionDetailResponseDTO auctionResponse = GsonParser.GSON.fromJson(
                                                GsonParser.GSON.toJsonTree(response.getData()),
                                                AuctionDetailResponseDTO.class
                                        );
                                        Platform.runLater(() -> {
                                            UserSession.setAuctionDetail(auctionResponse);
                                            UserSession.setAuctionId(targetAuctionId);
                                            liveAuctionController controller = ViewManager.changeViewWithController("liveAuction.fxml");
                                            if (controller != null) {
                                                controller.setUpPreviewImg(images);
                                                controller.setInvisible();
                                            }
                                        });
                                    } else {
                                        Platform.runLater(() -> {
                                            if (ControllerManager.getDashboardController() != null) {
                                                ControllerManager.getDashboardController().showToast("FAILED", response.getMessage(), false);
                                            }
                                        });
                                    }
                                });
                    } else {
                        Platform.runLater(() -> {
                            if (ControllerManager.getDashboardController() != null) {
                                ControllerManager.getDashboardController().showToast("FAILED", joinRoomResponse.getMessage(), false);
                            }
                        });
                    }
                });
    }
}
