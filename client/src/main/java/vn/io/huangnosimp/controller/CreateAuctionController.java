package vn.io.huangnosimp.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import vn.io.huangnosimp.Manager.ControllerManager;
import vn.io.huangnosimp.Manager.SocketManager;
import vn.io.huangnosimp.Manager.UserSession;
import vn.io.huangnosimp.Manager.ViewManager;
import vn.io.huangnosimp.dto.request.CreateAuctionRequestDTO;
import vn.io.huangnosimp.dto.response.AuctionCardDTO;
import vn.io.huangnosimp.dto.response.CloudinaryUploadSignatureResponseDTO;
import vn.io.huangnosimp.dto.shared.ItemAttributesDTO;
import vn.io.huangnosimp.enums.ItemCondition;
import vn.io.huangnosimp.enums.ItemType;
import vn.io.huangnosimp.protocol.ActionType;
import vn.io.huangnosimp.protocol.Request;
import vn.io.huangnosimp.protocol.ResponseStatus;
import vn.io.huangnosimp.util.GsonParser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class CreateAuctionController implements Initializable {

    // ── Form fields ───────────────────────────────────────
    @FXML private TextField itemNameField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private ChoiceBox<String> conditionChoice;
    @FXML private TextArea descriptionArea;

    // Art attributes
    @FXML private VBox artAttributes;
    @FXML private TextField artistField;
    @FXML private TextField creationYearField;

    // Electronics attributes
    @FXML private VBox electronicsAttributes;
    @FXML private TextField brandField;
    @FXML private Spinner<Integer> warrantySpinner;

    // Vehicle attributes
    @FXML private VBox vehicleAttributes;
    @FXML private ComboBox<String> engineTypeCombo;
    @FXML private TextField mileageField;

    // Pricing
    @FXML private TextField startPriceField;
    @FXML private ComboBox<String> startPriceUnitCombo;
    @FXML private VBox buyNowBox;
    @FXML private CheckBox buyNowCheck;
    @FXML private TextField buyNowPriceField;
    @FXML private ComboBox<String> buyNowUnitCombo;
    @FXML private Label buyNowEquivalentLabel;
    @FXML private TextField bidIncrementField;
    @FXML private ComboBox<String> bidIncrementUnitCombo;

    // Schedule
    @FXML private DatePicker startDatePicker;
    @FXML private TextField startTimeField;
    @FXML private ComboBox<String> durationCombo;

    // Image upload
    @FXML private Button btnUpload;
    @FXML private FlowPane flowPaneThumbnails;

    // ── Upload state ──────────────────────────────────────
    private List<File> selectedFiles  = new ArrayList<>();
    private List<String> uploadedUrls = Collections.synchronizedList(new ArrayList<>());
    private int totalFilesToUpload    = 0;

    // ── Initialize ────────────────────────────────────────

    @Override
    public void initialize(URL location, ResourceBundle resources) {}

    // ── Category change ───────────────────────────────────

    @FXML
    private void handleCategoryChange() {
        String selected = categoryCombo.getValue();

        artAttributes.setVisible(false);          artAttributes.setManaged(false);
        electronicsAttributes.setVisible(false);  electronicsAttributes.setManaged(false);
        vehicleAttributes.setVisible(false);      vehicleAttributes.setManaged(false);

        switch (selected) {
            case "Art"         -> { artAttributes.setVisible(true);        artAttributes.setManaged(true); }
            case "Electronics" -> { electronicsAttributes.setVisible(true); electronicsAttributes.setManaged(true); }
            case "Vehicle"     -> { vehicleAttributes.setVisible(true);     vehicleAttributes.setManaged(true); }
        }
    }

    // ── Buy now toggle ────────────────────────────────────

    @FXML
    private void handleBuyNowToggle() {
        boolean enabled = buyNowCheck.isSelected();
        buyNowBox.setVisible(enabled);
        buyNowBox.setManaged(enabled);
    }

    // ── Image upload ──────────────────────────────────────

    @FXML
    public void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn các ảnh sản phẩm");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        List<File> files = fileChooser.showOpenMultipleDialog(btnUpload.getScene().getWindow());
        if (files == null || files.isEmpty()) return;

        // Kiểm tra kích thước từng file
        for (File file : files) {
            if (file.length() > 5 * 1024 * 1024) {
                ControllerManager.getDashboardController()
                        .showToast("FAILED", "File " + file.getName() + " vượt quá 5MB!", false);
                return;
            }
        }

        this.selectedFiles = files;
        renderPreviews(files);
    }

    private void renderPreviews(List<File> files) {
        Platform.runLater(() -> {
            flowPaneThumbnails.getChildren().clear();
            for (File file : files) {
                ImageView iv = new ImageView(new Image(file.toURI().toString()));
                iv.setFitWidth(100);
                iv.setFitHeight(100);
                iv.setPreserveRatio(true);
                flowPaneThumbnails.getChildren().add(iv);
            }
        });
    }

    // ── Launch button ─────────────────────────────────────

    @FXML
    private void handleLaunchButton() {
        if (!validate()) return;

        ItemAttributesDTO attributes = buildAttributes();
        if (attributes == null) return;

        ItemCondition condition = getItemCondition();
        if (condition == null) return;

        long   startTime    = calculateStartTime();
        long   endTime      = calculateEndTime(startTime);
        double startPrice   = calculateRealPrice(startPriceField, startPriceUnitCombo);
        double bidIncrement = calculateRealPrice(bidIncrementField, bidIncrementUnitCombo);
        double buyNowPrice  = buyNowPriceField.getText().isBlank() ? 0
                : calculateRealPrice(buyNowPriceField, buyNowUnitCombo);

        // Không có ảnh → gửi luôn
        if (selectedFiles.isEmpty()) {
            sendCreateAuction(attributes, condition,
                    startTime, endTime,
                    startPrice, bidIncrement,
                    buyNowPrice, List.of());
            return;
        }

        // Có ảnh → xin signature → upload → gửi CREATE_AUCTION trong callback
        totalFilesToUpload = selectedFiles.size();
        uploadedUrls.clear();

        Request request = new Request(ActionType.GET_CLOUDINARY_UPLOAD_SIGNATURE, null);
        SocketManager.getClient().sendRequestAsync(request)
                .thenAccept(response -> {
                    if (!ResponseStatus.SUCCESS.equals(response.getStatus())) {
                        Platform.runLater(() ->
                                ControllerManager.getDashboardController()
                                        .showToast("FAILED", "Không lấy được signature bảo mật", false)
                        );
                        return;
                    }

                    CloudinaryUploadSignatureResponseDTO sig = GsonParser.GSON.fromJson(
                            GsonParser.GSON.toJsonTree(response.getData()),
                            CloudinaryUploadSignatureResponseDTO.class
                    );

                    Platform.runLater(() ->
                            ControllerManager.getDashboardController()
                                    .showToast("SUCCESS", "Đang tải " + selectedFiles.size() + " ảnh lên...", true)
                    );

                    // Upload từng file, callback chỉ chạy khi TẤT CẢ đã xong
                    for (File file : selectedFiles) {
                        uploadToCloudinary(file, sig, totalFilesToUpload,
                                () -> sendCreateAuction(
                                        attributes, condition,
                                        startTime, endTime,
                                        startPrice, bidIncrement,
                                        buyNowPrice,
                                        new ArrayList<>(uploadedUrls) // snapshot tại thời điểm đủ ảnh
                                )
                        );
                    }
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    Platform.runLater(() ->
                            ControllerManager.getDashboardController()
                                    .showToast("FAILED", "Lỗi kết nối khi xin signature", false)
                    );
                    return null;
                });
    }

    // ── Upload to Cloudinary ──────────────────────────────

    /**
     * Upload 1 file lên Cloudinary bất đồng bộ.
     * Khi tất cả file đã upload xong (uploadedUrls.size() == totalFiles),
     * callback onAllDone sẽ được gọi để tiếp tục gửi CREATE_AUCTION.
     */
    private void uploadToCloudinary(File file,
                                    CloudinaryUploadSignatureResponseDTO sig,
                                    int totalFiles,
                                    Runnable onAllDone) {
        CompletableFuture.runAsync(() -> {
            try {
                String uploadUrl  = sig.getUploadUrl();
                HttpClient client = HttpClient.newHttpClient();
                String boundary   = "----Boundary" + System.currentTimeMillis();

                // Build multipart body
                ByteArrayOutputStream body = new ByteArrayOutputStream();
                byte[] fileBytes = Files.readAllBytes(file.toPath());

                body.write(("--" + boundary + "\r\n").getBytes());
                body.write(("Content-Disposition: form-data; name=\"file\"; filename=\""
                        + file.getName() + "\"\r\n").getBytes());
                body.write(("Content-Type: application/octet-stream\r\n\r\n").getBytes());
                body.write(fileBytes);
                body.write("\r\n".getBytes());

                writeField(body, boundary, "api_key",   sig.getApiKey());
                writeField(body, boundary, "timestamp", String.valueOf(sig.getTimestamp()));
                writeField(body, boundary, "signature", sig.getSignature());
                writeField(body, boundary, "folder",    sig.getFolder());
                body.write(("--" + boundary + "--\r\n").getBytes());

                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(uploadUrl))
                        .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                        .POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray()))
                        .build();

                HttpResponse<String> httpResponse = client.send(
                        httpRequest, HttpResponse.BodyHandlers.ofString()
                );

                if (httpResponse.statusCode() == 200) {
                    JsonObject json = JsonParser.parseString(httpResponse.body()).getAsJsonObject();
                    String secureUrl = json.get("secure_url").getAsString();
                    uploadedUrls.add(secureUrl);

                    // Tất cả file đã upload xong → gọi callback
                    if (uploadedUrls.size() == totalFiles) {
                        Platform.runLater(() ->
                                ControllerManager.getDashboardController()
                                        .showToast("SUCCESS", "Đã tải lên " + totalFiles + " ảnh!", true)
                        );
                        onAllDone.run(); // ← gọi sendCreateAuction()
                    }
                } else {
                    throw new Exception("Cloudinary error: " + httpResponse.body());
                }

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->
                        ControllerManager.getDashboardController()
                                .showToast("FAILED", "Lỗi khi tải file " + file.getName(), false)
                );
            }
        });
    }

    // ── Send CREATE_AUCTION ───────────────────────────────

    /**
     * Gửi request tạo phiên đấu giá sau khi đã có đầy đủ imageUrls.
     * Hàm này chỉ được gọi từ callback của uploadToCloudinary().
     */
    private void sendCreateAuction(ItemAttributesDTO attributes,
                                   ItemCondition condition,
                                   long startTime, long endTime,
                                   double startPrice, double bidIncrement,
                                   double buyNowPrice, List<String> imageUrls) {
        CreateAuctionRequestDTO dto = new CreateAuctionRequestDTO(
                itemNameField.getText().trim(),
                descriptionArea.getText().trim(),
                ItemType.valueOf(categoryCombo.getValue().toUpperCase()),
                attributes,
                startPrice,
                startTime,
                endTime,
                condition,
                bidIncrement,
                buyNowPrice,
                imageUrls // ← đầy đủ URL từ Cloudinary
        );

        SocketManager.getClient().sendRequestAsync(new Request(ActionType.CREATE_AUCTION, dto))
                .thenAccept(response -> Platform.runLater(() -> {
                    if (ResponseStatus.SUCCESS.equals(response.getStatus())) {
                        AuctionCardDTO card = GsonParser.GSON.fromJson(
                                GsonParser.GSON.toJsonTree(response.getData()),
                                AuctionCardDTO.class
                        );
                        ControllerManager.getDashboardHomeController()
                                .addToDashboard(card, "My",
                                        ControllerManager.getDashboardHomeController().getAuctionFlowPane());
                        UserSession.addoneMyCard(card);
                        ControllerManager.getDashboardHomeController().changeTab();
                        ViewManager.changeView("dashboard_home.fxml", 1);
                    } else {
                        showError(response.getMessage());
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> showError("Không thể kết nối đến máy chủ."));
                    return null;
                });
    }

    // ── Helpers ───────────────────────────────────────────

    private void writeField(ByteArrayOutputStream body, String boundary,
                            String name, String value) throws IOException {
        body.write(("--" + boundary + "\r\n").getBytes());
        body.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n").getBytes());
        body.write((value + "\r\n").getBytes());
    }

    private boolean validate() {
        if (itemNameField.getText().isBlank()) {
            showAlert("ERROR", "Vui lòng nhập tên sản phẩm."); return false;
        }
        if (categoryCombo.getValue() == null) {
            showAlert("ERROR", "Vui lòng chọn category."); return false;
        }
        if (startPriceField.getText().isBlank()) {
            showAlert("ERROR", "Vui lòng nhập giá khởi điểm."); return false;
        }
        if (startDatePicker.getValue() == null) {
            showAlert("ERROR", "Vui lòng chọn ngày bắt đầu."); return false;
        }
        if (startTimeField.getText().isBlank()) {
            showAlert("ERROR", "Vui lòng nhập giờ bắt đầu."); return false;
        }
        if (!buyNowPriceField.getText().isBlank()
                && buyNowPriceField.getText().equals("0")) {
            showAlert("ERROR", "Buy now price không được bằng 0."); return false;
        }
        return true;
    }

    private ItemCondition getItemCondition() {
        return switch (conditionChoice.getValue()) {
            case "New"      -> ItemCondition.NEW;
            case "Like New" -> ItemCondition.LIKE_NEW;
            case "Used"     -> ItemCondition.USED;
            default         -> null;
        };
    }

    private ItemAttributesDTO buildAttributes() {
        return switch (categoryCombo.getValue()) {
            case "Art" -> {
                if (artistField.getText().isBlank()) {
                    showError("Vui lòng nhập tên nghệ sĩ."); yield null;
                }
                yield ItemAttributesDTO.createArtAttributes(
                        artistField.getText().trim(),
                        Integer.parseInt(creationYearField.getText().trim())
                );
            }
            case "Electronics" -> {
                if (brandField.getText().isBlank()) {
                    showError("Vui lòng nhập tên thương hiệu."); yield null;
                }
                yield ItemAttributesDTO.createElectronicsAttributes(
                        brandField.getText().trim(),
                        warrantySpinner.getValue()
                );
            }
            case "Vehicle" -> {
                if (engineTypeCombo.getValue() == null) {
                    showError("Vui lòng chọn loại động cơ."); yield null;
                }
                yield ItemAttributesDTO.createVehicleAttributes(
                        engineTypeCombo.getValue(),
                        Integer.parseInt(mileageField.getText().trim())
                );
            }
            default -> null;
        };
    }

    private double calculateRealPrice(TextField field, ComboBox<String> unitCombo) {
        double value = Double.parseDouble(field.getText().trim());
        return switch (unitCombo.getValue()) {
            case "Thousand" -> value * 1_000;
            case "Million"  -> value * 1_000_000;
            case "Billion"  -> value * 1_000_000_000;
            default         -> value;
        };
    }

    private long calculateStartTime() {
        LocalDate date   = startDatePicker.getValue();
        String[] parts   = startTimeField.getText().split(":");
        int hour = parts.length > 0 ? Integer.parseInt(parts[0].trim()) : 0;
        int min  = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 0;
        return LocalDateTime.of(date, LocalTime.of(hour, min))
                .toInstant(ZoneOffset.of("+07:00"))
                .toEpochMilli();
    }

    private long calculateEndTime(long startTime) {
        long duration = switch (durationCombo.getValue()) {
            case "15 minutes" -> 900_000L;
            case "30 minutes" -> 1_800_000L;
            case "45 minutes" -> 2_700_000L;
            case "1 hour"     -> 3_600_000L;
            case "3 hours"    -> 10_800_000L;
            case "6 hours"    -> 21_600_000L;
            case "12 hours"   -> 43_200_000L;
            case "1 day"      -> 86_400_000L;
            case "3 days"     -> 259_200_000L;
            case "7 days"     -> 604_800_000L;
            default           -> 86_400_000L;
        };
        return startTime + duration;
    }

    private void showError(String msg) {
        Platform.runLater(() -> showAlert("ERROR", msg));
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    public void handleCancelButton(ActionEvent event) {
        ViewManager.changeView("dashboard_home.fxml", 1);
    }
}