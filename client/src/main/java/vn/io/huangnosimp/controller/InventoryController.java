package vn.io.huangnosimp.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import vn.io.huangnosimp.Manager.ControllerManager;
import vn.io.huangnosimp.Manager.FormatUtil;
import vn.io.huangnosimp.Manager.TimeSyncManager;
import vn.io.huangnosimp.dto.response.AuctionCardDTO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InventoryController {
    @FXML private VBox listingsContainer;
    @FXML private VBox wonContainer;

    @FXML private Label lblTotalItems;
    @FXML private Label lblListingCount;
    @FXML private Label lblLiveCount;
    @FXML private Label lblWonCount;
    @FXML private Label lblTotalSpent;

    @FXML private Button filterAllListings;
    @FXML private Button filterLive;
    @FXML private Button filterEnded;
    @FXML private Button filterCancelled;

    @FXML private Button filterAllWon;
    @FXML private Button filterPending;
    @FXML private Button filterReceived;

    @FXML private VBox listingsEmpty;
    @FXML private VBox wonEmpty;

    private List<AuctionCardDTO> allListings = new ArrayList<>();
    private List<AuctionCardDTO> wonList = new ArrayList<>();

    private String currentListingFilter = "ALL";
    private String currentWonFilter = "ALL";

    @FXML
    public void initialize() {
        ControllerManager.setInventoryController(this);
        setupFilterActions();
    }

    public void setData(List<AuctionCardDTO> listings, List<AuctionCardDTO> wonItems) {
        this.allListings = listings != null ? listings : new ArrayList<>();
        this.wonList = wonItems != null ? wonItems : new ArrayList<>();

        updateStatistics();
        applyFiltersAndRender();
    }

    private void updateStatistics() {
        long now = TimeSyncManager.nowMillis();
        int totalListings = allListings.size();
        long liveCount = allListings.stream()
                .filter(dto -> dto.getEndTime() > now && dto.getStartTime() <= now)
                .count();
        int totalWon = wonList.size();
        double totalSpent = wonList.stream()
                .mapToDouble(AuctionCardDTO::getYourBid)
                .sum();

        lblTotalItems.setText((totalListings + totalWon) + " items");
        lblListingCount.setText(String.valueOf(totalListings));
        lblLiveCount.setText(String.valueOf(liveCount));
        lblWonCount.setText(String.valueOf(totalWon));
        lblTotalSpent.setText(FormatUtil.formatNumber(totalSpent));
    }

    private void setupFilterActions() {
        filterAllListings.setOnAction(e -> handleListingFilterChange("ALL", filterAllListings));
        filterLive.setOnAction(e -> handleListingFilterChange("LIVE", filterLive));
        filterEnded.setOnAction(e -> handleListingFilterChange("ENDED", filterEnded));
        filterCancelled.setOnAction(e -> handleListingFilterChange("CANCELLED", filterCancelled));

        filterAllWon.setOnAction(e -> handleWonFilterChange("ALL", filterAllWon));
        filterPending.setOnAction(e -> handleWonFilterChange("PENDING", filterPending));
        filterReceived.setOnAction(e -> handleWonFilterChange("RECEIVED", filterReceived));
    }

    private void handleListingFilterChange(String filter, Button clickedButton) {
        currentListingFilter = filter;
        
        // Reset styles for all listing filter buttons
        filterAllListings.getStyleClass().remove("inv-filter-active");
        filterLive.getStyleClass().remove("inv-filter-active");
        filterEnded.getStyleClass().remove("inv-filter-active");
        filterCancelled.getStyleClass().remove("inv-filter-active");

        // Set active style to clicked button
        clickedButton.getStyleClass().add("inv-filter-active");

        renderListings();
    }

    private void handleWonFilterChange(String filter, Button clickedButton) {
        currentWonFilter = filter;

        // Reset styles for all won filter buttons
        filterAllWon.getStyleClass().remove("inv-filter-active");
        filterPending.getStyleClass().remove("inv-filter-active");
        filterReceived.getStyleClass().remove("inv-filter-active");

        // Set active style to clicked button
        clickedButton.getStyleClass().add("inv-filter-active");

        renderWonItems();
    }

    private void applyFiltersAndRender() {
        renderListings();
        renderWonItems();
    }

    private void renderListings() {
        listingsContainer.getChildren().clear();
        long now = TimeSyncManager.nowMillis();

        List<AuctionCardDTO> filtered = allListings.stream().filter(dto -> {
            switch (currentListingFilter) {
                case "LIVE":
                    return dto.getEndTime() > now && dto.getStartTime() <= now;
                case "ENDED":
                    return dto.getEndTime() <= now;
                case "CANCELLED":
                    return false; // placeholder or handle cancellation if field available
                default:
                    return true;
            }
        }).collect(Collectors.toList());

        if (filtered.isEmpty()) {
            listingsEmpty.setVisible(true);
            listingsEmpty.setManaged(true);
        } else {
            listingsEmpty.setVisible(false);
            listingsEmpty.setManaged(false);
            for (AuctionCardDTO dto : filtered) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Listingrowitem.fxml"));
                    Parent node = loader.load();
                    RowItemController controller = loader.getController();
                    controller.setUpListingRow(dto);
                    listingsContainer.getChildren().add(node);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void renderWonItems() {
        wonContainer.getChildren().clear();

        List<AuctionCardDTO> filtered = wonList.stream().filter(dto -> {
            switch (currentWonFilter) {
                case "PENDING":
                    return false; // placeholder, adjust if logic/data supports pending status
                case "RECEIVED":
                    return false; // placeholder
                default:
                    return true;
            }
        }).collect(Collectors.toList());

        if (filtered.isEmpty()) {
            wonEmpty.setVisible(true);
            wonEmpty.setManaged(true);
        } else {
            wonEmpty.setVisible(false);
            wonEmpty.setManaged(false);
            for (AuctionCardDTO dto : filtered) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Wonrowitem.fxml"));
                    Parent node = loader.load();
                    RowItemController controller = loader.getController();
                    controller.setUpWonRow(dto);
                    wonContainer.getChildren().add(node);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Keep existing methods for backward compatibility
    public void addMyItem(List<AuctionCardDTO> myList) {
        this.allListings = myList != null ? myList : new ArrayList<>();
        updateStatistics();
        applyFiltersAndRender();
    }

    public void addWonAuction(List<AuctionCardDTO> wonList) {
        this.wonList = wonList != null ? wonList : new ArrayList<>();
        updateStatistics();
        applyFiltersAndRender();
    }
}
