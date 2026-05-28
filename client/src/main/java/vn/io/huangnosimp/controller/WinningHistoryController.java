package vn.io.huangnosimp.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;

import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;



public class WinningHistoryController implements Initializable {
    public enum DeliveryStatus {
        PENDING,
        DELIVERED,
        COMPLETED
    }

    public static class WonAuctionItem {

        private final int            id;
        private final String         itemName;
        private final String         category;
        private final String         sellerName;
        private final String         lotCode;
        private final double         winningBid;
        private final double         startingPrice;
        private final int            participantCount;
        private final LocalDateTime  wonAt;
        private       DeliveryStatus deliveryStatus;
        private final String         thumbnailEmoji;

        public WonAuctionItem(int id, String itemName, String category,
                              String sellerName, String lotCode,
                              double winningBid, double startingPrice,
                              int participantCount, LocalDateTime wonAt,
                              DeliveryStatus deliveryStatus, String thumbnailEmoji) {
            this.id               = id;
            this.itemName         = itemName;
            this.category         = category;
            this.sellerName       = sellerName;
            this.lotCode          = lotCode;
            this.winningBid       = winningBid;
            this.startingPrice    = startingPrice;
            this.participantCount = participantCount;
            this.wonAt            = wonAt;
            this.deliveryStatus   = deliveryStatus;
            this.thumbnailEmoji   = thumbnailEmoji;
        }

        public int            getId()               { return id; }
        public String         getItemName()         { return itemName; }
        public String         getCategory()         { return category; }
        public String         getSellerName()       { return sellerName; }
        public String         getLotCode()          { return lotCode; }
        public double         getWinningBid()       { return winningBid; }
        public double         getStartingPrice()    { return startingPrice; }
        public int            getParticipantCount() { return participantCount; }
        public LocalDateTime  getWonAt()            { return wonAt; }
        public DeliveryStatus getDeliveryStatus()   { return deliveryStatus; }
        public String         getThumbnailEmoji()   { return thumbnailEmoji; }
        public void           setDeliveryStatus(DeliveryStatus s) { this.deliveryStatus = s; }
    }


    //  FXML BINDINGS

    @FXML private Label            lblResultCount;
    @FXML private Label            lblTotalWon;
    @FXML private Label            lblTotalSpent;
    @FXML private Label            lblAvgBid;
    @FXML private Label            lblPendingDelivery;

    @FXML private TextField        txtSearch;
    @FXML private ToggleButton     filterAll;
    @FXML private ToggleButton     filterPending;
    @FXML private ToggleButton     filterDelivered;
    @FXML private ToggleButton     filterCompleted;
    @FXML private ComboBox<String> cbSort;

    @FXML private VBox             itemList;
    @FXML private ScrollPane       scrollPane;
    @FXML private Button           btnShowMore;


    //  STATE

    private final List<WonAuctionItem> allItems     = new ArrayList<>();
    private final List<WonAuctionItem> visibleItems = new ArrayList<>();

    private static final int PAGE_SIZE = 6;
    private int loadedCount = 0;

    private static final DateTimeFormatter DATE_FMT  =
            DateTimeFormatter.ofPattern("dd MMM yyyy  ·  HH:mm");
    private static final NumberFormat      MONEY_FMT =
            NumberFormat.getCurrencyInstance(Locale.US);


    //   INITIALIZE
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadDummyData();
        setupSortCombo();
        setupSearchListener();
        setupFilterButtons();
        applyFilters();
    }


    //  DUMMY DATA  –  thay bằng service / DAO thực tế

    private void loadDummyData() {
        allItems.addAll(List.of(
                new WonAuctionItem(1, "iPhone 15 Pro Max 256GB – Space Black",
                        "Electronics", "TechStore Official", "#A-2024-081",
                        2850, 2000, 14,
                        LocalDateTime.of(2026, 4, 17, 18, 55),
                        DeliveryStatus.DELIVERED, "📱"),

                new WonAuctionItem(2, "Original Watercolor – Sunset Series No.3",
                        "Art", "ArtGallery Saigon", "#B-2024-033",
                        1200, 500, 8,
                        LocalDateTime.of(2026, 4, 15, 11, 0),
                        DeliveryStatus.PENDING, "🎨"),

                new WonAuctionItem(3, "Toyota Camry 2022 – Pearl White",
                        "Vehicle", "AutoHouse Premium", "#C-2025-007",
                        28500, 25000, 22,
                        LocalDateTime.of(2026, 4, 10, 9, 30),
                        DeliveryStatus.COMPLETED, "🚗"),

                new WonAuctionItem(4, "MacBook Pro M3 14\" – Midnight",
                        "Electronics", "Apple Reseller VN", "#A-2025-102",
                        1950, 1500, 17,
                        LocalDateTime.of(2026, 4, 8, 14, 20),
                        DeliveryStatus.DELIVERED, "💻"),

                new WonAuctionItem(5, "Antique Ceramic Vase – Qing Dynasty Replica",
                        "Art", "HeritageBid VN", "#D-2024-055",
                        750, 300, 11,
                        LocalDateTime.of(2026, 3, 29, 16, 45),
                        DeliveryStatus.PENDING, "🏺"),

                new WonAuctionItem(6, "Rolex Submariner – Stainless Steel 40mm",
                        "Luxury", "WatchVault SG", "#E-2025-019",
                        9800, 8000, 30,
                        LocalDateTime.of(2026, 3, 22, 20, 0),
                        DeliveryStatus.COMPLETED, "⌚"),

                new WonAuctionItem(7, "Sony A7R V – Full Frame Mirrorless Kit",
                        "Electronics", "CameraZone HCM", "#A-2025-088",
                        3200, 2600, 9,
                        LocalDateTime.of(2026, 3, 15, 13, 10),
                        DeliveryStatus.DELIVERED, "📷"),

                new WonAuctionItem(8, "Gaming PC RTX 4090 Custom Build",
                        "Electronics", "TechBuild Pro", "#A-2025-141",
                        4500, 3800, 19,
                        LocalDateTime.of(2026, 3, 5, 17, 0),
                        DeliveryStatus.PENDING, "🖥️")
        ));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  SETUP
    // ═══════════════════════════════════════════════════════════════════════════

    private void setupSortCombo() {
        cbSort.getItems().addAll(
                "Newest first", "Oldest first",
                "Highest bid",  "Lowest bid",
                "Seller name A–Z");
        cbSort.setValue("Newest first");
        cbSort.setOnAction(e -> applyFilters());
    }

    private void setupSearchListener() {
        txtSearch.textProperty().addListener((obs, o, n) -> applyFilters());
    }

    private void setupFilterButtons() {
        ToggleGroup tg = new ToggleGroup();
        filterAll.setToggleGroup(tg);
        filterPending.setToggleGroup(tg);
        filterDelivered.setToggleGroup(tg);
        filterCompleted.setToggleGroup(tg);
        filterAll.setSelected(true);

        // Không cho bỏ chọn tất cả các nút
        tg.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT == null) filterAll.setSelected(true);
            applyFilters();
        });
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  FILTER / SORT / RENDER
    // ═══════════════════════════════════════════════════════════════════════════

    private void applyFilters() {
        String keyword = txtSearch.getText().trim().toLowerCase();

        List<WonAuctionItem> filtered = allItems.stream()
                .filter(this::matchesStatusFilter)
                .filter(item -> keyword.isEmpty()
                        || item.getItemName().toLowerCase().contains(keyword)
                        || item.getSellerName().toLowerCase().contains(keyword)
                        || item.getLotCode().toLowerCase().contains(keyword))
                .collect(Collectors.toList());

        String sort = cbSort.getValue();
        if (sort != null) switch (sort) {
            case "Newest first"    -> filtered.sort(Comparator.comparing(WonAuctionItem::getWonAt).reversed());
            case "Oldest first"    -> filtered.sort(Comparator.comparing(WonAuctionItem::getWonAt));
            case "Highest bid"     -> filtered.sort(Comparator.comparingDouble(WonAuctionItem::getWinningBid).reversed());
            case "Lowest bid"      -> filtered.sort(Comparator.comparingDouble(WonAuctionItem::getWinningBid));
            case "Seller name A–Z" -> filtered.sort(Comparator.comparing(WonAuctionItem::getSellerName));
        }

        visibleItems.clear();
        visibleItems.addAll(filtered);
        loadedCount = 0;
        renderItems(false);
        updateStats();
    }

    private boolean matchesStatusFilter(WonAuctionItem item) {
        if (filterPending.isSelected())   return item.getDeliveryStatus() == DeliveryStatus.PENDING;
        if (filterDelivered.isSelected()) return item.getDeliveryStatus() == DeliveryStatus.DELIVERED;
        if (filterCompleted.isSelected()) return item.getDeliveryStatus() == DeliveryStatus.COMPLETED;
        return true;
    }

    /**
     * @param append true = Show More (thêm vào cuối), false = render lại từ đầu
     */
    private void renderItems(boolean append) {
        if (!append) {
            itemList.getChildren().clear();
            loadedCount = 0;
        }

        int from = loadedCount;
        int to   = Math.min(loadedCount + PAGE_SIZE, visibleItems.size());
        for (int i = from; i < to; i++) {
            itemList.getChildren().add(buildItemCard(visibleItems.get(i)));
        }
        loadedCount = to;

        boolean hasMore = loadedCount < visibleItems.size();
        btnShowMore.setVisible(hasMore);
        btnShowMore.setManaged(hasMore);
        lblResultCount.setText("Showing " + loadedCount + " of " + visibleItems.size() + " items");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  BUILD CARD
    // ═══════════════════════════════════════════════════════════════════════════

    private HBox buildItemCard(WonAuctionItem item) {
        HBox card = new HBox(14);
        card.getStyleClass().add("wa-item-card");
        card.setAlignment(Pos.CENTER_LEFT);

        // Thumbnail
        VBox thumb = new VBox();
        thumb.getStyleClass().add("wa-thumb");
        thumb.setAlignment(Pos.CENTER);
        thumb.setPrefSize(72, 72);
        thumb.setMaxSize(72, 72);
        Label thumbIcon = new Label(item.getThumbnailEmoji());
        thumbIcon.getStyleClass().add("wa-thumb-icon");
        thumb.getChildren().add(thumbIcon);

        // Main info
        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);

        HBox nameRow = new HBox(8);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label(item.getItemName());
        nameLabel.getStyleClass().add("wa-item-name");
        nameRow.getChildren().addAll(nameLabel, buildStatusBadge(item.getDeliveryStatus()));

        HBox meta1 = buildMetaRow(
                "Seller",   item.getSellerName(),
                "Lot",      item.getLotCode(),
                "Category", item.getCategory());

        HBox meta2 = buildMetaRow(
                "Won on",       item.getWonAt().format(DATE_FMT),
                "Participants", item.getParticipantCount() + " bidders",
                null, null);

        info.getChildren().addAll(nameRow, meta1, meta2);

        // Winning bid
        VBox amountBox = new VBox(4);
        amountBox.setAlignment(Pos.CENTER_RIGHT);
        amountBox.setPrefWidth(140);
        Label amtLabel = new Label("WINNING BID");
        amtLabel.getStyleClass().add("wa-amount-label");
        Label amtValue = new Label(MONEY_FMT.format(item.getWinningBid()));
        amtValue.getStyleClass().add("wa-amount-value");
        Label amtSub = new Label("Starting: " + MONEY_FMT.format(item.getStartingPrice()));
        amtSub.getStyleClass().add("wa-amount-sub");
        amountBox.getChildren().addAll(amtLabel, amtValue, amtSub);

        // Action buttons
        VBox actionBox = new VBox(6);
        actionBox.setAlignment(Pos.CENTER);
        actionBox.setPrefWidth(120);

        Button btnDetail = new Button("View Details");
        btnDetail.getStyleClass().add("wa-btn-detail");
        btnDetail.setMaxWidth(Double.MAX_VALUE);
        btnDetail.setOnAction(e -> handleViewDetails(item));

        Button btnContact = new Button("Contact Seller");
        btnContact.getStyleClass().add("wa-btn-contact");
        btnContact.setMaxWidth(Double.MAX_VALUE);
        btnContact.setOnAction(e -> handleContactSeller(item));

        actionBox.getChildren().addAll(btnDetail, btnContact);
        card.getChildren().addAll(thumb, info, amountBox, actionBox);
        return card;
    }

    private Label buildStatusBadge(DeliveryStatus status) {
        Label badge = new Label();
        badge.getStyleClass().add("wa-status-badge");
        switch (status) {
            case PENDING   -> { badge.setText(" Pending");   badge.getStyleClass().add("wa-status-pending");   }
            case DELIVERED -> { badge.setText(" Delivered");  badge.getStyleClass().add("wa-status-delivered"); }
            case COMPLETED -> { badge.setText(" Completed"); badge.getStyleClass().add("wa-status-completed"); }
        }
        return badge;
    }

    private HBox buildMetaRow(String k1, String v1,
                              String k2, String v2,
                              String k3, String v3) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        addMetaPair(row, k1, v1);
        if (k2 != null) { addSep(row); addMetaPair(row, k2, v2); }
        if (k3 != null) { addSep(row); addMetaPair(row, k3, v3); }
        return row;
    }

    private void addMetaPair(HBox row, String key, String val) {
        Label k = new Label(key + ":");  k.getStyleClass().add("wa-meta-key");
        Label v = new Label(val);        v.getStyleClass().add("wa-meta-val");
        row.getChildren().addAll(k, v);
    }

    private void addSep(HBox row) {
        Label sep = new Label("·");
        sep.getStyleClass().add("wa-meta-sep");
        row.getChildren().add(sep);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  STATS
    // ═══════════════════════════════════════════════════════════════════════════

    private void updateStats() {
        long   totalWon   = allItems.size();
        double totalSpent = allItems.stream().mapToDouble(WonAuctionItem::getWinningBid).sum();
        double avgBid     = totalWon > 0 ? totalSpent / totalWon : 0;
        long   pending    = allItems.stream()
                .filter(i -> i.getDeliveryStatus() == DeliveryStatus.PENDING).count();

        lblTotalWon.setText(String.valueOf(totalWon));
        lblTotalSpent.setText(MONEY_FMT.format(totalSpent));
        lblAvgBid.setText(MONEY_FMT.format(avgBid));
        lblPendingDelivery.setText(String.valueOf(pending));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  FXML HANDLERS
    // ═══════════════════════════════════════════════════════════════════════════

    @FXML
    private void handleShowMore() {
        renderItems(true);
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  ACTION HANDLERS  –  mở rộng theo nghiệp vụ
    // ═══════════════════════════════════════════════════════════════════════════

    private void handleViewDetails(WonAuctionItem item) {
        System.out.println("[WonAuctions] View details: " + item.getLotCode() + " – " + item.getItemName());
        // TODO: DashboardController.getInstance().loadView("won_auction_detail.fxml", item);
    }

    private void handleContactSeller(WonAuctionItem item) {
        System.out.println("[WonAuctions] Contact seller: " + item.getSellerName());
        // TODO: mở chat / email dialog
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  PUBLIC API  –  gọi từ DashboardController sau khi fetch data thực tế
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Nạp dữ liệu từ ngoài vào (thay thế dummy data).
     * Gọi trước khi scene được hiển thị.
     */
    public void loadItems(List<WonAuctionItem> items) {
        allItems.clear();
        allItems.addAll(items);
        Platform.runLater(() -> {
            applyFilters();
            updateStats();
        });
    }
}
