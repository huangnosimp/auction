package vn.io.huangnosimp.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class AuctionListController implements Initializable {

    // ---- Stats ----
    @FXML private Label lblOpenCount;
    @FXML private Label lblJoinedCount;
    @FXML private Label lblTotalBid;
    @FXML private Label lblSoonest;

    // ---- Filter ----
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterOrg;
    @FXML private ComboBox<String> filterSlot;
    @FXML private ComboBox<String> filterStatus;

    // ---- Tabs ----
    @FXML private Button tabAll;
    @FXML private Button tabJoined;
    @FXML private Button tabNotJoined;
    @FXML private Button tabEndingSoon;
    @FXML private Label  lblResultCount;

    // ---- Table ----
    @FXML private TableView<AuctionItem>         auctionTable;
    @FXML private TableColumn<AuctionItem, Integer> colIndex;
    @FXML private TableColumn<AuctionItem, String>  colFight;
    @FXML private TableColumn<AuctionItem, String>  colOrg;
    @FXML private TableColumn<AuctionItem, String>  colSlot;
    @FXML private TableColumn<AuctionItem, String>  colRemaining;
    @FXML private TableColumn<AuctionItem, String>  colCurrentBid;
    @FXML private TableColumn<AuctionItem, String>  colYourBid;
    @FXML private TableColumn<AuctionItem, Integer> colBidders;
    @FXML private TableColumn<AuctionItem, String>  colStatus;
    @FXML private TableColumn<AuctionItem, String>  colAction;

    // ---- Data ----
    private ObservableList<AuctionItem> masterData;
    private FilteredList<AuctionItem>   filteredData;
    private String currentTab = "ALL";

    // ============================================================
    //  Data Model
    // ============================================================
    public static class AuctionItem {
        private final int    index;
        private final String fight, org, slot, remaining, currentBid, yourBid;
        private final int    bidders;
        private final String status, action;
        private final boolean joined;

        public AuctionItem(int index, String fight, String org, String slot,
                           String remaining, String currentBid, String yourBid,
                           int bidders, String status, String action, boolean joined) {
            this.index = index; this.fight = fight; this.org = org;
            this.slot = slot; this.remaining = remaining;
            this.currentBid = currentBid; this.yourBid = yourBid;
            this.bidders = bidders; this.status = status;
            this.action = action; this.joined = joined;
        }

        public int    getIndex()      { return index; }
        public String getFight()      { return fight; }
        public String getOrg()        { return org; }
        public String getSlot()       { return slot; }
        public String getRemaining()  { return remaining; }
        public String getCurrentBid() { return currentBid; }
        public String getYourBid()    { return yourBid; }
        public int    getBidders()    { return bidders; }
        public String getStatus()     { return status; }
        public String getAction()     { return action; }
        public boolean isJoined()     { return joined; }
    }

    // ============================================================
    //  Initialize
    // ============================================================
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblTotalBid.setText("$12,750");

        filterOrg.setItems(FXCollections.observableArrayList("UFC", "PFL", "Bellator"));
        filterSlot.setItems(FXCollections.observableArrayList("Main show", "Pre-show", "Post-show"));
        filterStatus.setItems(FXCollections.observableArrayList("Winning", "Loosing", "Tham gia", "Chưa tham gia"));

        setupColumns();
        loadData();
        setupSearch();
    }

    // ============================================================
    //  Columns
    // ============================================================
    private void setupColumns() {
        colIndex.setCellValueFactory(new PropertyValueFactory<>("index"));
        colIndex.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.valueOf(item));
                setStyle("-fx-text-fill: #5a5e72; -fx-alignment: CENTER;");
            }
        });

        colFight.setCellValueFactory(new PropertyValueFactory<>("fight"));

        // Org badge
        colOrg.setCellValueFactory(new PropertyValueFactory<>("org"));
        colOrg.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label badge = new Label(item);
                badge.getStyleClass().add(switch (item) {
                    case "UFC"      -> "org-ufc";
                    case "PFL"      -> "org-pfl";
                    case "Bellator" -> "org-bellator";
                    default         -> "badge-open";
                });
                setGraphic(badge); setText(null);
            }
        });

        colSlot.setCellValueFactory(new PropertyValueFactory<>("slot"));

        // Remaining — red if ending soon
        colRemaining.setCellValueFactory(new PropertyValueFactory<>("remaining"));
        colRemaining.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(item);
                getStyleClass().removeAll("time-warning", "time-normal");
                getStyleClass().add(item.startsWith("0:") ? "time-warning" : "time-normal");
            }
        });

        colCurrentBid.setCellValueFactory(new PropertyValueFactory<>("currentBid"));
        colCurrentBid.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setStyle("-fx-alignment: CENTER_RIGHT; -fx-text-fill: #f0f2f8; -fx-font-weight: bold;");
            }
        });

        colYourBid.setCellValueFactory(new PropertyValueFactory<>("yourBid"));
        colYourBid.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setStyle("-fx-alignment: CENTER_RIGHT; -fx-text-fill: #8b8fa8;");
            }
        });

        colBidders.setCellValueFactory(new PropertyValueFactory<>("bidders"));
        colBidders.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item + " người");
                setStyle("-fx-alignment: CENTER; -fx-text-fill: #8b8fa8;");
            }
        });

        // Status badge
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isEmpty()) { setGraphic(null); return; }
                Label badge = new Label(item);
                badge.getStyleClass().add(switch (item) {
                    case "Winning"  -> "badge-winning";
                    case "Loosing"  -> "badge-losing";
                    case "Tham gia" -> "badge-participate";
                    default         -> "badge-open";
                });
                setGraphic(badge); setText(null);
            }
        });

        // Action button
        colAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        colAction.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Button btn = new Button(item);
                btn.getStyleClass().add(switch (item) {
                    case "Chỉnh sửa"  -> "btn-edit";
                    case "Tham gia"   -> "btn-join";
                    case "Đặt thầu"   -> "btn-make-bid";
                    case "No bids"    -> "btn-no-bids";
                    default           -> "btn-edit";
                });
                btn.setOnAction(e -> handleActionClick(item, getTableRow().getItem()));
                setGraphic(btn); setText(null);
            }
        });
    }

    // ============================================================
    //  Data
    // ============================================================
    private void loadData() {
        masterData = FXCollections.observableArrayList(
            new AuctionItem( 1, "UFC 300: Pereira vs. Hill",           "UFC",      "Main show", "1:45:30", "$2,100", "$2,100",  8, "Winning",  "Chỉnh sửa", true),
            new AuctionItem( 2, "UFC 302: Makhachev vs. Poirier",      "UFC",      "Main show", "0:30:12", "$4,300", "$3,800",  5, "Loosing",  "No bids",   true),
            new AuctionItem( 3, "UFC 308: Topuria vs. Holloway",       "UFC",      "Main show", "4:15:55", "$2,100", "—",       3, "Tham gia", "Đặt thầu",  false),
            new AuctionItem( 4, "UFC 309: Jones vs. Miocic",           "UFC",      "Main show", "1:45:30", "$2,100", "$2,100",  6, "Winning",  "Chỉnh sửa", true),
            new AuctionItem( 5, "UFC 310: Pantoja vs. Asakura",        "UFC",      "Pre-show",  "1:45:30", "$2,100", "$2,100",  4, "Winning",  "Chỉnh sửa", true),
            new AuctionItem( 6, "UFC 311: Makhachev vs. Tsarukyan 2",  "UFC",      "Main show", "0:30:12", "$4,300", "$2,100",  7, "Loosing",  "No bids",   true),
            new AuctionItem( 7, "UFC 312: Du Plessis vs. Strickland 2","UFC",      "Main show", "1:45:30", "$2,100", "$2,100",  5, "Winning",  "Chỉnh sửa", true),
            new AuctionItem( 8, "PFL: Ngannou vs. Ferreira",           "PFL",      "Main show", "4:15:55", "$1,800", "—",       2, "Tham gia", "Đặt thầu",  false),
            new AuctionItem( 9, "Bellator: Nurmagomedov vs. Shably",   "Bellator", "Main show", "1:45:30", "$2,100", "$2,100",  3, "Winning",  "Chỉnh sửa", true),
            new AuctionItem(10, "UFC Fight Night: Covington vs. Buckley","UFC",    "Pre-show",  "1:45:30", "$2,100", "$2,100",  4, "Winning",  "Chỉnh sửa", true),
            new AuctionItem(11, "PFL Playoffs: Larissa Pacheco",        "PFL",      "Main show", "6:00:00", "$1,500", "—",       1, "Tham gia", "Tham gia",  false),
            new AuctionItem(12, "Bellator: Pitbull vs. Primus 3",      "Bellator", "Post-show", "3:20:00", "$900",  "—",        2, "Tham gia", "Tham gia",  false)
        );

        filteredData = new FilteredList<>(masterData, p -> true);
        auctionTable.setItems(filteredData);
        updateResultCount();
    }

    // ============================================================
    //  Search & Filter
    // ============================================================
    private void setupSearch() {
        searchField.textProperty().addListener((obs, o, n) -> applyFilter());
        filterOrg.valueProperty().addListener((obs, o, n) -> applyFilter());
        filterSlot.valueProperty().addListener((obs, o, n) -> applyFilter());
        filterStatus.valueProperty().addListener((obs, o, n) -> applyFilter());
    }

    private void applyFilter() {
        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        String org    = filterOrg.getValue();
        String slot   = filterSlot.getValue();
        String status = filterStatus.getValue();

        filteredData.setPredicate(item -> {
            if (!search.isEmpty() && !item.getFight().toLowerCase().contains(search)) return false;
            if (org    != null && !item.getOrg().equals(org))       return false;
            if (slot   != null && !item.getSlot().equals(slot))     return false;
            if (status != null && !item.getStatus().equals(status)) return false;
            return switch (currentTab) {
                case "JOINED"     ->  item.isJoined();
                case "NOT_JOINED" -> !item.isJoined();
                case "ENDING"     ->  item.getRemaining().startsWith("0:");
                default           -> true;
            };
        });
        updateResultCount();
    }

    private void updateResultCount() {
        lblResultCount.setText("Hiển thị " + filteredData.size() + " kết quả");
    }

    // ============================================================
    //  Tab handler
    // ============================================================
    @FXML
    private void handleTab(javafx.event.ActionEvent e) {
        Button src = (Button) e.getSource();
        for (Button b : new Button[]{tabAll, tabJoined, tabNotJoined, tabEndingSoon}) {
            b.getStyleClass().removeAll("tab-active");
        }
        src.getStyleClass().add("tab-active");

        currentTab = switch (src.getId()) {
            case "tabJoined"     -> "JOINED";
            case "tabNotJoined"  -> "NOT_JOINED";
            case "tabEndingSoon" -> "ENDING";
            default              -> "ALL";
        };
        applyFilter();
    }

    // ============================================================
    //  Reset filter
    // ============================================================
    @FXML
    private void handleReset() {
        searchField.clear();
        filterOrg.setValue(null);
        filterSlot.setValue(null);
        filterStatus.setValue(null);
        currentTab = "ALL";
        for (Button b : new Button[]{tabAll, tabJoined, tabNotJoined, tabEndingSoon}) {
            b.getStyleClass().removeAll("tab-active");
        }
        tabAll.getStyleClass().add("tab-active");
        applyFilter();
    }

    // ============================================================
    //  Action button click — navigate to auction room
    // ============================================================
    private void handleActionClick(String action, AuctionItem item) {
        if (item == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/AuctionRoom.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) auctionTable.getScene().getWindow();
            stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
        } catch (Exception ex) {
            System.err.println("Không thể mở phòng đấu giá: " + ex.getMessage());
        }
    }
}
