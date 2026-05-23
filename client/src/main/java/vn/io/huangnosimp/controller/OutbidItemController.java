package vn.io.huangnosimp.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import vn.io.huangnosimp.Manager.ControllerManager;
import vn.io.huangnosimp.Manager.SocketManager;
import vn.io.huangnosimp.Manager.UserSession;
import vn.io.huangnosimp.Manager.ViewManager;
import vn.io.huangnosimp.dto.request.GetAuctionDetailRequestDTO;
import vn.io.huangnosimp.dto.request.JoinRoomRequestDTO;
import vn.io.huangnosimp.dto.response.AuctionCardDTO;
import vn.io.huangnosimp.dto.response.AuctionDetailResponseDTO;
import vn.io.huangnosimp.protocol.ActionType;
import vn.io.huangnosimp.protocol.Request;
import vn.io.huangnosimp.protocol.ResponseStatus;
import vn.io.huangnosimp.util.GsonParser;

import java.math.BigDecimal;

import static vn.io.huangnosimp.Manager.FormatUtil.formatNumber;


public class OutbidItemController {
    @FXML private Label lblProductTitle;
    @FXML private Label lblTopBid;
    @FXML private Button btnQuickBid;

    private String auctionId;

    public void setUp(AuctionCardDTO dto){
        this.auctionId = dto.getAuctionId();
        lblProductTitle.setText(dto.getProductName());
        lblTopBid.setText(formatNumber(calculatePrice(dto.getCurrentPrice()))+" VND");
    }

    public void setUpManual(String auctionId, String productName, double currentPrice){
        this.auctionId = auctionId;
        lblProductTitle.setText(productName);
        lblTopBid.setText(formatNumber(calculatePrice(currentPrice))+" VND");
    }

    @FXML
    private void handleQuickBid(ActionEvent event){
        if (auctionId == null) return;
        btnQuickBid.setDisable(true);

        JoinRoomRequestDTO joinRoomRequest = new JoinRoomRequestDTO(auctionId);
        Request request = new Request(ActionType.JOIN_ROOM, joinRoomRequest);

        SocketManager.getClient().sendRequestAsync(request)
            .thenAccept(response -> {
                if (ResponseStatus.SUCCESS.equals(response.getStatus())) {
                    GetAuctionDetailRequestDTO getDetail = new GetAuctionDetailRequestDTO(auctionId);
                    SocketManager.getClient().sendRequestAsync(new Request(ActionType.GET_AUCTION_DETAIL, getDetail))
                        .thenAccept(detailResponse -> {
                            if (ResponseStatus.SUCCESS.equals(detailResponse.getStatus())) {
                                AuctionDetailResponseDTO auctionDetail = GsonParser.GSON.fromJson(
                                    GsonParser.GSON.toJsonTree(detailResponse.getData()),
                                    AuctionDetailResponseDTO.class
                                );
                                Platform.runLater(() -> {
                                    UserSession.setAuctionDetail(auctionDetail);
                                    UserSession.setAuctionId(auctionId);
                                    ControllerManager.getDashboardController().removeOutbidItem(auctionId);
                                    ViewManager.changeViewWithController("liveAuction.fxml");
                                    btnQuickBid.setDisable(false);
                                });
                            } else {
                                Platform.runLater(() -> btnQuickBid.setDisable(false));
                            }
                        });
                } else {
                    Platform.runLater(() -> {
                        ControllerManager.getDashboardController().showToast("FAILED", response.getMessage(), false);
                        btnQuickBid.setDisable(false);
                    });
                }
            });
    }

    private double calculatePrice(double value) {
        return Double.parseDouble(BigDecimal.valueOf(value).toPlainString());
    }
}
