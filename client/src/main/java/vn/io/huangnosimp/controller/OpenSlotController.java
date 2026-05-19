package vn.io.huangnosimp.controller;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.util.Duration;
import vn.io.huangnosimp.Manager.ControllerManager;
import vn.io.huangnosimp.Manager.SocketManager;
import vn.io.huangnosimp.Manager.UserSession;
import vn.io.huangnosimp.dto.request.GetPublicAcutionCardDTO;
import vn.io.huangnosimp.dto.response.AuctionCardDTO;
import vn.io.huangnosimp.dto.response.GetPublicAuctionCardResponseDTO;
import vn.io.huangnosimp.protocol.ActionType;
import vn.io.huangnosimp.protocol.Request;
import vn.io.huangnosimp.protocol.ResponseStatus;
import vn.io.huangnosimp.util.GsonParser;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;

public class OpenSlotController implements Initializable {
    @FXML private FlowPane cardContainer;
    @FXML private TextField txtSearch;

    public FlowPane getCardContainer(){
        return cardContainer;
    }

    public void clearContainer(){
        getCardContainer().getChildren().clear();
    }

    public void addCard(AuctionCardDTO dto, String type){
        String fxmlPath = null;
        if(type.equals("Joining")){
            fxmlPath = "/fxml/JoiningauctionCard.fxml";
        }
        else if (type.equals("My")) {
            fxmlPath = "/fxml/MyauctionCard.fxml";
        }
        else{
            fxmlPath = "/fxml/PublicauctionCard.fxml";
        }

        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent cardNode = loader.load();
            ItemCardController cardController = loader.getController();
            cardController.addInfo(dto, type);
            cardNode.setUserData(dto.getAuctionId());
            cardContainer.getChildren().add(cardNode);
            FadeTransition ft = new FadeTransition(Duration.millis(500), cardNode);
            ft.setFromValue(0.0);
            ft.setToValue(1.0);
            ft.play();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
    public void clearCard(String auctionId){
        Node nodeToRemove = cardContainer.getChildren().stream()
                .filter(node -> auctionId.equals(node.getUserData()))
                .findFirst()
                .orElse(null);


        if (nodeToRemove != null) {
            FadeTransition ft = new FadeTransition(Duration.millis(300), nodeToRemove);
            ft.setFromValue(1.0);
            ft.setToValue(0.0);

            ft.setOnFinished(event -> {
                cardContainer.getChildren().remove(nodeToRemove);
            });

            ft.play();
        }
    }
    public void setupOpenSlot(List<AuctionCardDTO> listcard){
        for(AuctionCardDTO dto : listcard){
            addCard(dto, "Public");
        }
    }

    @FXML
    public void handleSearchItem(){

    }
    @FXML
    public void loadMorePublicCards() {
        if(txtSearch.getText() == null || txtSearch.getText().trim().isEmpty()) {
            Set<String> existingIds = new HashSet<>();
            UserSession.getListCard().forEach(c -> existingIds.add(c.getAuctionId()));
            UserSession.getJoiningListCard().forEach(c -> existingIds.add(c.getAuctionId()));
            UserSession.getMyListCard().forEach(c -> existingIds.add(c.getAuctionId()));

            List<AuctionCardDTO> result = new ArrayList<>();
            fetchMoreUntilFull(result, existingIds, 30, 5);
        }
    }

    private void fetchMoreUntilFull(List<AuctionCardDTO> result,
                                    Set<String> existingIds,
                                    int target,
                                    int maxRetry) {

        if (result.size() >= target || maxRetry <= 0) {
            applyResult(result);
            return;
        }

        int needed = target - result.size();
        GetPublicAcutionCardDTO requestDTO = new GetPublicAcutionCardDTO(needed * 2);
        Request request = new Request(ActionType.GET_PUBLIC_AUCTION_CARD, requestDTO);

        SocketManager.getClient().sendRequestAsync(request)
                .thenAccept(response -> {
                    if (!ResponseStatus.SUCCESS.equals(response.getStatus())) {
                        applyResult(result);
                        return;
                    }

                    GetPublicAuctionCardResponseDTO dto = GsonParser.GSON.fromJson(
                            GsonParser.GSON.toJsonTree(response.getData()),
                            GetPublicAuctionCardResponseDTO.class
                    );

                    List<AuctionCardDTO> responseList = dto.getPublicAuctionCardList();

                    if (responseList == null || responseList.isEmpty()) {
                        applyResult(result);
                        return;
                    }

                    for (AuctionCardDTO card : responseList) {
                        if (!existingIds.contains(card.getAuctionId())) {
                            result.add(card);
                            existingIds.add(card.getAuctionId());
                            if (result.size() >= target) break;
                        }
                    }

                    if (result.size() >= target) {
                        applyResult(result);
                    } else {
                        fetchMoreUntilFull(result, existingIds, target, maxRetry - 1);
                    }
                });
    }

    // Tách riêng để tránh lặp code Platform.runLater
    private void applyResult(List<AuctionCardDTO> result) {
        Platform.runLater(() -> {
            UserSession.getListCard().addAll(result); // thêm vào session, không xóa
            for (AuctionCardDTO card : result) {
                addCard(card, "Public"); // gọi trực tiếp trong controller
            }
        });
    }
    public void initialize(URL location, ResourceBundle resources){
        ControllerManager.setOpenSlotController(this);
        setupOpenSlot(UserSession.getListCard());
    }
}
