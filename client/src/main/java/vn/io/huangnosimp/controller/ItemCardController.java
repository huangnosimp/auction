package vn.io.huangnosimp.controller;

import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import javafx.event.ActionEvent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import vn.io.huangnosimp.Manager.*;
import vn.io.huangnosimp.dto.request.GetAuctionDetailRequestDTO;
import vn.io.huangnosimp.dto.request.GetPublicAcutionCardDTO;
import vn.io.huangnosimp.dto.request.JoinRoomRequestDTO;
import vn.io.huangnosimp.dto.response.AuctionCardDTO;
import vn.io.huangnosimp.dto.response.AuctionDetailResponseDTO;
import vn.io.huangnosimp.protocol.ActionType;
import vn.io.huangnosimp.protocol.Request;
import vn.io.huangnosimp.protocol.ResponseStatus;
import vn.io.huangnosimp.util.GsonParser;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.List;

import static vn.io.huangnosimp.Manager.FormatUtil.formatNumber;
import static vn.io.huangnosimp.Manager.ViewManager.*;

public class ItemCardController {
    @FXML private Label lblProductName;
    @FXML private Label lblTimeRemaining;
    @FXML private Label lblCurrentBid;
    @FXML private Label statuslbl;

    @FXML private Label lblBidderCount;
    @FXML private Label lblBidCount;
    @FXML private Label lblYourBid;

    @FXML private Button btnManage;

    @FXML private ImageView imgProduct;

    private String auctionId;
    private List<String> displayImg;

    public void addInfo(AuctionCardDTO dto, String type){
        displayImg = dto.getImageUrl();
        imgProduct.setImage(new Image(displayImg.get(0), 0, 0, true, true));
        lblProductName.setText(dto.getProductName());
        long now = Instant.now().toEpochMilli();
        if(now < dto.getStartTime()){
            statuslbl.setText("Start in: ");
        }
        else {
            statuslbl.setText("End in: ");
        }
        AuctionCountdownUtil countdownUtil = new AuctionCountdownUtil(lblTimeRemaining, dto.getStartTime(), dto.getEndTime());
        countdownUtil.start();
        lblCurrentBid.setText(caculateCurrentBid(dto.getCurrentPrice()));
        this.auctionId = dto.getAuctionId();
        if(type.equals("Joining")){
            lblYourBid.setText(String.valueOf(dto.getYourBid())+" đ");
        }
        else if (type.equals("My")) {
            lblBidCount.setText(String.valueOf(dto.getBidCount())+" bids");
        }
        else if(type.equals("Public")){
            lblBidderCount.setText(String.valueOf(dto.getBidderCount())+" bidders");
        }
    }
    private String caculateCurrentBid(double value){
        if(value >= 1000000000){
            return formatNumber(value/1000000000)+" B";
        }
        else if (value>=1000000){
            return formatNumber(value/1000000)+" M";
        }
        else {
            return formatNumber(value/1000)+" K";
        }
    }
    @FXML
    public void handleBidNowbutton(ActionEvent event){
        JoinRoomRequestDTO joinRoomRequest = new JoinRoomRequestDTO(auctionId);

        Request request = new Request(ActionType.JOIN_ROOM, joinRoomRequest);

        SocketManager.getClient().sendRequestAsync(request)
                .thenAccept(response->{
                    if(ResponseStatus.SUCCESS.equals(response.getStatus())){

                        GetAuctionDetailRequestDTO getDetail = new GetAuctionDetailRequestDTO(auctionId);

                        SocketManager.getClient().sendRequestAsync(new Request(ActionType.GET_AUCTION_DETAIL, getDetail))
                                .thenAccept(Joinresponse -> {

                                    if(ResponseStatus.SUCCESS.equals(Joinresponse.getStatus())){

                                        AuctionDetailResponseDTO auctionResponse = GsonParser.GSON.fromJson(GsonParser.GSON.toJsonTree(Joinresponse.getData()), AuctionDetailResponseDTO.class);
                                        Platform.runLater(()->{
                                            UserSession.addonejoiningCard(UserSession.findWithId(auctionId));
                                            UserSession.setAuctionDetail(auctionResponse);
                                            UserSession.setAuctionId(auctionId);
                                            liveAuctionController controller = changeViewWithController("liveAuction.fxml");
                                            controller.setUpPreviewImg(displayImg);
                                            ControllerManager.getDashboardHomeController().addToDashboard(UserSession.findWithId(auctionId), "Joining", ControllerManager.getDashboardHomeController().getFlowJoined());
                                        });
                                    }
                                });
                        GetPublicAcutionCardDTO getPublicCard = new GetPublicAcutionCardDTO(1);
                        Request request2 = new Request(ActionType.GET_PUBLIC_AUCTION_CARD, getPublicCard);
                        SocketManager.getClient().sendRequestAsync(request2)
                                .thenAccept(response1 -> {
                                    if (ResponseStatus.SUCCESS.equals(response1.getStatus())) {
                                        Type listType = new TypeToken<List<AuctionCardDTO>>(){}.getType();
                                        List<AuctionCardDTO> list = GsonParser.GSON.fromJson(GsonParser.GSON.toJsonTree(response1.getData()), listType);
                                        for(AuctionCardDTO dto:list){
                                            ControllerManager.getOpenSlotController().addCard(dto, "Public");
                                        }
                                    }
                                });
                    }
                    if(ResponseStatus.FAILED.equals(response.getStatus())){
                        ControllerManager.getDashboardController().showToast("FAILED", response.getMessage(), false);
                    }
                });
    }
    @FXML
    public void handleEnterRoom(){
        GetAuctionDetailRequestDTO getDetail = new GetAuctionDetailRequestDTO(auctionId);
        SocketManager.getClient().sendRequestAsync(new Request(ActionType.GET_AUCTION_DETAIL, getDetail))
                .thenAccept(response -> {
                    if(ResponseStatus.SUCCESS.equals(response.getStatus())){
                        AuctionDetailResponseDTO auctionResponse = GsonParser.GSON.fromJson(GsonParser.GSON.toJsonTree(response.getData()), AuctionDetailResponseDTO.class);
                        Platform.runLater(()->{
                            UserSession.setAuctionDetail(auctionResponse);
                            UserSession.setAuctionId(auctionId);
                            liveAuctionController controller = changeViewWithController("liveAuction.fxml");
                            controller.setUpPreviewImg(displayImg);
                            if (btnManage.getText().equals("Manage")) {
                                controller.setInvisible();
                            }
                        });
                    }
                    if(ResponseStatus.FAILED.equals(response.getStatus())){
                        ControllerManager.getDashboardController().showToast("FAILED", response.getMessage(), false);
                    }
                });
    }
}
