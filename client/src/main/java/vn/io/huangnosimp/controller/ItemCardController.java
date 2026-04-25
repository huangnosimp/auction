package vn.io.huangnosimp.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

import javafx.event.ActionEvent;
import vn.io.huangnosimp.Manager.SocketManager;
import vn.io.huangnosimp.dto.request.GetAuctionDetailRequestDTO;
import vn.io.huangnosimp.dto.response.AuctionCardDTO;
import vn.io.huangnosimp.dto.response.AuctionResponseDTO;
import vn.io.huangnosimp.protocol.ActionType;
import vn.io.huangnosimp.protocol.Request;
import vn.io.huangnosimp.protocol.ResponseStatus;
import vn.io.huangnosimp.util.GsonParser;

import java.io.IOException;

public class ItemCardController {
    @FXML private Label productNamelabel;
    @FXML private Label lblTime;
    @FXML private Label lblCurrentBid;
    @FXML private Label lblYourBid;
    private String auctionId;
    public void addInfo(AuctionCardDTO dto){
        productNamelabel.setText(dto.getProductName());
        lblTime.setText("");
        lblCurrentBid.setText(String.valueOf(dto.getCurrentPrice()));
        lblYourBid.setText(String.valueOf(dto.getYourBid()));
        this.auctionId = dto.getAuctionId();
    }
    @FXML public void handleBidbutton(ActionEvent event){
        GetAuctionDetailRequestDTO getDetail = new GetAuctionDetailRequestDTO(auctionId);
        SocketManager.getClient().sendRequestAsync(new Request(ActionType.GET_AUCTION_DETAIL, getDetail))
                .thenAccept(response -> Platform.runLater(()->{
                    if(ResponseStatus.SUCCESS.equals(response.getStatus())){
                        AuctionResponseDTO auctionResponse = GsonParser.GSON.fromJson(GsonParser.GSON.toJsonTree(response.getData()), AuctionResponseDTO.class);

                    }
                }));
    }
}
