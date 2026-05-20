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
import java.text.Normalizer;
import java.util.regex.Pattern;

public class OpenSlotController implements Initializable {
    @FXML private FlowPane cardContainer;
    @FXML private TextField txtSearch;
    private String lastSearchWord = "";
    private static final Pattern DIACRITICS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

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
    public void handleSearch() {
        Set<String> existingIds = new HashSet<>();
        UserSession.getJoiningListCard().forEach(c -> existingIds.add(c.getAuctionId()));
        UserSession.getMyListCard().forEach(c -> existingIds.add(c.getAuctionId()));

        List<AuctionCardDTO> result = new ArrayList<>();
        fetchMoreUntilFull(result, existingIds, 30, 5, txtSearch.getText(), false);
    }
    @FXML
    public void loadMorePublicCards() {
        Set<String> existingIds = new HashSet<>();
        UserSession.getListCard().forEach(c -> existingIds.add(c.getAuctionId()));
        UserSession.getJoiningListCard().forEach(c -> existingIds.add(c.getAuctionId()));
        UserSession.getMyListCard().forEach(c -> existingIds.add(c.getAuctionId()));

        List<AuctionCardDTO> result = new ArrayList<>();
        fetchMoreUntilFull(result, existingIds, 30, 5, lastSearchWord, true);
    }



    public static String removeDiacritics(String str) {
        if (str == null) return "";
        String normalized = Normalizer.normalize(str, Normalizer.Form.NFD);
        return DIACRITICS_PATTERN.matcher(normalized).replaceAll("");
    }

    private void fetchMoreUntilFull(List<AuctionCardDTO> result,
                                    Set<String> existingIds,
                                    int target,
                                    int maxRetry,
                                    String searchName,
                                    boolean isLoadMore) {

        if (result.size() >= target || maxRetry <= 0) {
            applyResult(result, searchName, isLoadMore);
            return;
        }

        int needed = target - result.size();
        GetPublicAcutionCardDTO requestDTO = new GetPublicAcutionCardDTO(needed * 2);
        Request request = new Request(ActionType.GET_PUBLIC_AUCTION_CARD, requestDTO);

        SocketManager.getClient().sendRequestAsync(request)
                .thenAccept(response -> {
                    if (!ResponseStatus.SUCCESS.equals(response.getStatus())) {
                        applyResult(result, searchName, isLoadMore);
                        return;
                    }

                    GetPublicAuctionCardResponseDTO dto = GsonParser.GSON.fromJson(
                            GsonParser.GSON.toJsonTree(response.getData()),
                            GetPublicAuctionCardResponseDTO.class
                    );

                    List<AuctionCardDTO> responseList = dto.getPublicAuctionCardList();

                    if (responseList == null || responseList.isEmpty()) {
                        applyResult(result, searchName, isLoadMore);
                        return;
                    }

                    if (searchName != null && !searchName.trim().isEmpty()) {
                        String keyword = removeDiacritics(searchName.trim().toLowerCase());
                        for (AuctionCardDTO card : responseList) {
                            String cardName = removeDiacritics(card.getProductName().toLowerCase());
                            if (!existingIds.contains(card.getAuctionId())
                                    && cardName.contains(keyword)) {
                                result.add(card);
                                existingIds.add(card.getAuctionId());
                                if (result.size() >= target) break;
                            }
                        }
                    } else {
                        for (AuctionCardDTO card : responseList) {
                            if (!existingIds.contains(card.getAuctionId())) {
                                result.add(card);
                                existingIds.add(card.getAuctionId());
                                if (result.size() >= target) break;
                            }
                        }
                    }

                    if (result.size() >= target) {
                        applyResult(result, searchName, isLoadMore);
                    } else {
                        fetchMoreUntilFull(result, existingIds, target, maxRetry - 1,
                                searchName, isLoadMore);
                    }
                });
    }

    private void applyResult(List<AuctionCardDTO> result,
                             String searchName,
                             boolean isLoadMore) {
        Platform.runLater(() -> {
            if (!isLoadMore) {
                UserSession.getListCard().clear();
                clearContainer();
                lastSearchWord = (searchName == null) ? "" : searchName.trim();
            }

            UserSession.getListCard().addAll(result);
            for (AuctionCardDTO card : result) {
                addCard(card, "Public");
            }
        });
    }
    public void initialize(URL location, ResourceBundle resources){
        ControllerManager.setOpenSlotController(this);
        setupOpenSlot(UserSession.getListCard());
    }
}
