package vn.io.huangnosimp.controller;

import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
import vn.io.huangnosimp.dto.response.PlaceBidResponseDTO;
import vn.io.huangnosimp.dto.shared.AuctionExtendedDTO;
import vn.io.huangnosimp.dto.shared.NotificationDTO;
import vn.io.huangnosimp.enums.NotificationType;
import vn.io.huangnosimp.network.IServerMessageListener;
import vn.io.huangnosimp.protocol.ActionType;
import vn.io.huangnosimp.protocol.Request;
import vn.io.huangnosimp.protocol.Response;
import vn.io.huangnosimp.protocol.ResponseStatus;
import vn.io.huangnosimp.util.GsonParser;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static vn.io.huangnosimp.Manager.FormatUtil.formatNumber;
import static vn.io.huangnosimp.Manager.UserSession.getUsername;
import static vn.io.huangnosimp.Manager.ViewManager.*;

public class ItemCardController implements Initializable, IServerMessageListener {
    @FXML private Label lblProductName;
    @FXML private Label lblTimeRemaining;
    @FXML private Label lblCurrentBid;
    @FXML private Label statuslbl;

    @FXML private Label lblBidderCount;
    @FXML private Label lblBidCount;
    @FXML private Label lblYourBid;
    @FXML private Label lblBidStatus;
    @FXML private Label lblItemType;

    @FXML private Button btnManage;

    @FXML private ImageView imgProduct;

    private int bidCount;
    private int bidderCount;

    private String auctionId;
    private List<String> displayImg;
    private AuctionCountdownUtil countdownUtil;

    public void addInfo(AuctionCardDTO dto, String type){
        displayImg = dto.getImageUrl();
        bidCount = dto.getBidCount();
        bidderCount = dto.getBidderCount();
        this.auctionId = dto.getAuctionId();
        if (lblBidStatus != null) {
            if(dto.getCurrentPrice() == dto.getYourBid()){
                lblBidStatus.setText("WINNING");
                lblBidStatus.getStyleClass().removeAll("s-outbid");
                lblBidStatus.getStyleClass().add("s-winning");
            }
            else{
                lblBidStatus.setText("OUTBID");
                lblBidStatus.getStyleClass().removeAll("s-winning");
                lblBidStatus.getStyleClass().add("s-outbid");
            }
        }
        if (displayImg != null && !displayImg.isEmpty()) {
            imgProduct.setImage(new Image(displayImg.get(0), 0, 0, true, true));
        } else {
            imgProduct.setImage(null);
        }
        lblProductName.setText(dto.getProductName());
        lblItemType.setText("");
        long now = TimeSyncManager.nowMillis();
        if(now < dto.getStartTime()){
            statuslbl.setText("Start in: ");
        }
        else {
            statuslbl.setText("End in: ");
        }
        countdownUtil = new AuctionCountdownUtil(lblTimeRemaining, dto.getStartTime(), dto.getEndTime());
        countdownUtil.start();
        lblCurrentBid.setText(caculateCurrentBid(dto.getCurrentPrice()));
        if(type.equals("Joining")){
            lblYourBid.setText(caculateCurrentBid(dto.getYourBid()));
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
        JoinRoomRequestDTO joinRoomRequest = new JoinRoomRequestDTO(auctionId);

        Request request = new Request(ActionType.JOIN_ROOM, joinRoomRequest);

        SocketManager.getClient().sendRequestAsync(request)
                .thenAccept(joinRoomResponse -> {
                    if(ResponseStatus.SUCCESS.equals(joinRoomResponse.getStatus())){
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
                                            if (btnManage != null && btnManage.getText().equals("Manage")) {
                                                controller.setInvisible();
                                            }
                                        });
                                    }
                                    if(ResponseStatus.FAILED.equals(response.getStatus())){
                                        ControllerManager.getDashboardController().showToast("FAILED", response.getMessage(), false);
                                    }
                                });
                    }
                    if(ResponseStatus.FAILED.equals(joinRoomResponse.getStatus())){
                        ControllerManager.getDashboardController().showToast("FAILED", joinRoomResponse.getMessage(), false);
                    }
                });

    }
    public void onResponseReceived(Response response){}
    public void onResponseReceived(Response response, ActionType actionType){}
    public void onRequestReceived(Request notification){
        NotificationDTO notificationDTO = GsonParser.GSON.fromJson(GsonParser.GSON.toJsonTree(notification.getData()), NotificationDTO.class);
        NotificationType notificationType = notificationDTO.getNotificationType();

        switch (notificationType){
            case NEW_BID -> {
                if (notificationDTO.getAuctionId() != null && notificationDTO.getAuctionId().equals(auctionId)) {

                    PlaceBidResponseDTO dto = GsonParser.GSON.fromJson(GsonParser.GSON.toJsonTree(notificationDTO.getData()), PlaceBidResponseDTO.class);
                    Platform.runLater(()->{
                        bidCount++;
                        if (lblBidCount != null) {
                            lblBidCount.setText(String.valueOf(bidCount) + " bids");
                        }
                        if (lblCurrentBid != null) {
                            lblCurrentBid.setText(caculateCurrentBid(dto.getAmount()));
                        }
                        if (dto.getUsername() != null && dto.getUsername().equals(getUsername())) {
                            if (lblYourBid != null) {
                                lblYourBid.setText(caculateCurrentBid(dto.getAmount()));
                            }
                            if (lblBidStatus != null) {
                                lblBidStatus.setText("WINNING");
                                lblBidStatus.getStyleClass().removeAll("s-outbid");
                                lblBidStatus.getStyleClass().add("s-winning");
                            }
                        } else {
                            if (lblBidStatus != null) {
                                lblBidStatus.setText("OUTBID");
                                lblBidStatus.getStyleClass().removeAll("s-winning");
                                lblBidStatus.getStyleClass().add("s-outbid");
                            }
                        }
                    });
                }
            }

            case AUCTION_EXTENDED -> {
                if (notificationDTO.getAuctionId() != null && notificationDTO.getAuctionId().equals(auctionId)) {
                    AuctionExtendedDTO dto = GsonParser.GSON.fromJson(
                            GsonParser.GSON.toJsonTree(notificationDTO.getData()),
                            AuctionExtendedDTO.class
                    );
                    if (dto != null) {
                        Platform.runLater(() -> {
                            UserSession.updateAuctionEndTime(auctionId, dto.getNewEndTime());
                            if (countdownUtil != null) {
                                countdownUtil.updateEndTime(dto.getNewEndTime());
                            }
                            if (statuslbl != null) {
                                statuslbl.setText("End in: ");
                            }
                        });
                    }
                }
            }

            case OUTBID -> {
                if(notificationDTO.getAuctionId().equals(auctionId)){

                    String msg = (String) notificationDTO.getData();
                    double price = extractPrice(msg);
                    Platform.runLater(() -> {
                        if (lblCurrentBid != null) {
                            lblCurrentBid.setText(caculateCurrentBid(price));
                        }
                        if (lblBidStatus != null) {
                            lblBidStatus.setText("OUTBID");
                            lblBidStatus.getStyleClass().removeAll("s-winning");
                            lblBidStatus.getStyleClass().add("s-outbid");
                        }
                    });
                }
            }
        }
    }
    public void onDisconnected(String reason){}

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        SocketManager.getClient().addListener(this);
    }
    private double extractPrice(String message) {
        Pattern pattern = Pattern.compile("Current price is ([\\d.]+) in room");
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1));
        }

        throw new IllegalArgumentException("Không tìm thấy giá trong chuỗi: " + message);
    }
}
