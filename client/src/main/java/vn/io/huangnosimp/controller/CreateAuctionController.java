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
import vn.io.huangnosimp.dto.response.DashboardResponseDTO;
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

public class CreateAuctionController implements Initializable{
    @FXML private TextField itemNameField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private ChoiceBox<String> conditionChoice;
    @FXML private TextArea descriptionArea;

    // Art
    @FXML private VBox artAttributes;
    @FXML private TextField artistField;
    @FXML private TextField creationYearField;

    // Electronics
    @FXML private VBox electronicsAttributes;
    @FXML private TextField brandField;
    @FXML private Spinner<Integer> warrantySpinner;

    // Vehicle
    @FXML private VBox vehicleAttributes;
    @FXML private ComboBox<String> engineTypeCombo;
    @FXML private TextField mileageField;

    // Pricing
    @FXML private TextField startPriceField;
    @FXML private VBox buyNowBox;
    @FXML private CheckBox buyNowCheck;
    @FXML private TextField buyNowPriceField;
    @FXML private ComboBox<String> buyNowUnitCombo;
    @FXML private Label buyNowEquivalentLabel;
    @FXML private TextField bidIncrementField;

    // Schedule
    @FXML private DatePicker startDatePicker;
    @FXML private TextField startTimeField;
    @FXML private ComboBox<String> durationCombo;

    @FXML private ComboBox<String> startPriceUnitCombo;
    @FXML private ComboBox<String> bidIncrementUnitCombo;

    @FXML private Button btnUpload;
    @FXML private FlowPane flowPaneThumbnails;

    private List<File> selectedFiles = new ArrayList<>();
    private List<String> uploadedUrls = Collections.synchronizedList(new ArrayList<>());
    private int totalFilesToUpload = 0;

    @FXML
    private void handleCategoryChange() {
        String selected = categoryCombo.getValue();

        artAttributes.setVisible(false);         artAttributes.setManaged(false);
        electronicsAttributes.setVisible(false);  electronicsAttributes.setManaged(false);
        vehicleAttributes.setVisible(false);      vehicleAttributes.setManaged(false);

        switch (selected) {
            case "Art"         -> { artAttributes.setVisible(true);         artAttributes.setManaged(true); }
            case "Electronics" -> { electronicsAttributes.setVisible(true);  electronicsAttributes.setManaged(true); }
            case "Vehicle"     -> { vehicleAttributes.setVisible(true);      vehicleAttributes.setManaged(true); }
        }
    }
    @FXML
    private void handleBuyNowToggle() {
        boolean enabled = buyNowCheck.isSelected();
        buyNowBox.setVisible(enabled);
        buyNowBox.setManaged(enabled);
    }


    @FXML
    public void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn các ảnh sản phẩm");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        List<File> files = fileChooser.showOpenMultipleDialog(btnUpload.getScene().getWindow());
        if (files == null || files.isEmpty()) return;
        for (File file : files) {
            if (file.length() > 5 * 1024 * 1024) {
                ControllerManager.getDashboardController().showToast("FAILED", "File " + file.getName() + " vượt quá 5MB!", false);
                return;
            }
        }
        this.selectedFiles = files;
        renderPreviews(files);
    }

    private void getSignature() {
        Request request = new Request(ActionType.GET_CLOUDINARY_UPLOAD_SIGNATURE, null);
        SocketManager.getClient().sendRequestAsync(request)
                .thenAccept(response -> {
                    if (ResponseStatus.SUCCESS.equals(response.getStatus())) {
                        CloudinaryUploadSignatureResponseDTO sig = GsonParser.GSON.fromJson(GsonParser.GSON.toJsonTree(response.getData()), CloudinaryUploadSignatureResponseDTO.class);

                        uploadedUrls.clear();
                        for (File file : selectedFiles) {
                            uploadToCloudinary(file, sig, totalFilesToUpload);
                        }

                        Platform.runLater(() ->
                                ControllerManager.getDashboardController().showToast("SUCCESS", "Đang bắt đầu tải " + selectedFiles.size() + " ảnh lên...", true)
                        );
                    } else {
                        Platform.runLater(() ->
                                ControllerManager.getDashboardController().showToast("FAILED", "Không lấy được signature bảo mật", false)
                        );
                    }
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
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
    private void uploadToCloudinary(File file, CloudinaryUploadSignatureResponseDTO sig, int totalFiles) {
        this.totalFilesToUpload = totalFiles;

        CompletableFuture.runAsync(() -> {
            try {
                String uploadUrl = "https://api.cloudinary.com/v1_1/" + sig.getCloudName() + "/image/upload";
                HttpClient httpClient = HttpClient.newHttpClient();
                String boundary = "----Boundary" + System.currentTimeMillis();

                // Chuẩn bị dữ liệu Body
                ByteArrayOutputStream body = new ByteArrayOutputStream();
                byte[] fileBytes = Files.readAllBytes(file.toPath());

                // Ghi file vào multipart body
                body.write(("--" + boundary + "\r\n").getBytes());
                body.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\n").getBytes());
                body.write(("Content-Type: application/octet-stream\r\n\r\n").getBytes());
                body.write(fileBytes);
                body.write("\r\n".getBytes());

                // Ghi các field bảo mật (API Key, Signature...)
                writeField(body, boundary, "api_key", sig.getApiKey());
                writeField(body, boundary, "timestamp", String.valueOf(sig.getTimestamp()));
                writeField(body, boundary, "signature", sig.getSignature());
                writeField(body, boundary, "folder", sig.getFolder());
                body.write(("--" + boundary + "--\r\n").getBytes());

                // Tạo Request
                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(uploadUrl))
                        .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                        .POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray()))
                        .build();

                // Gửi và nhận phản hồi
                HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

                if (httpResponse.statusCode() == 200) {
                    JsonObject json = JsonParser.parseString(httpResponse.body()).getAsJsonObject();
                    String secureUrl = json.get("secure_url").getAsString();

                    // Lưu URL vào danh sách dùng chung
                    uploadedUrls.add(secureUrl);

                    // KIỂM TRA: Nếu số lượng URL thu được bằng đúng số lượng file ban đầu
                    if (uploadedUrls.size() == totalFilesToUpload) {
                        Platform.runLater(() -> {
                            ControllerManager.getDashboardController().showToast("SUCCESS", "Đã tải lên toàn bộ " + totalFilesToUpload + " ảnh thành công!", true);
                            // Bạn có thể gọi logic tiếp theo ở đây, ví dụ: lưu dữ liệu vào DB
                        });
                    }
                } else {
                    throw new Exception("Lỗi từ Cloudinary: " + httpResponse.body());
                }

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->
                        ControllerManager.getDashboardController().showToast("FAILED", "Lỗi khi tải file " + file.getName() + ": " + e.getMessage(), false)
                );
            }
        });
    }

    // Helper ghi field vào multipart body
    private void writeField(ByteArrayOutputStream body, String boundary,
                            String name, String value) throws IOException {
        body.write(("--" + boundary + "\r\n").getBytes());
        body.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n").getBytes());
        body.write((value + "\r\n").getBytes());
    }

    @FXML
    private void handleLaunchButton() {
        if (!validate()) return;

        ItemAttributesDTO attributes = buildAttributes();
        if (attributes == null) return;

        ItemCondition condition = getItemCondition();
        if(condition==null) return;

        long startTime = calculateStartTime();
        long endTime   = calculateEndTime(startTime);

        double startPrice = calculateRealPrice(startPriceField, startPriceUnitCombo);
        double bidIncrement = calculateRealPrice(bidIncrementField, bidIncrementUnitCombo);
        double buyNowPrice;
        if(buyNowPriceField.getText().equals("0")){
            showAlert("wrong number","buy now price cannot be 0");
        }
        if(buyNowPriceField.getText().isBlank()){
            buyNowPrice=0;
        }
        else{
            buyNowPrice = calculateRealPrice(buyNowPriceField, buyNowUnitCombo);
        }
        getSignature();

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
                uploadedUrls
        );

        SocketManager.getClient().sendRequestAsync(new Request(ActionType.CREATE_AUCTION, dto))
                .thenAccept(response -> Platform.runLater(() -> {
                    if (ResponseStatus.SUCCESS.equals(response.getStatus())) {
                        AuctionCardDTO cardResponse = GsonParser.GSON.fromJson(GsonParser.GSON.toJsonTree(response.getData()), AuctionCardDTO.class);
                        ControllerManager.getDashboardHomeController().addToDashboard(cardResponse, "My", ControllerManager.getDashboardHomeController().getAuctionFlowPane());
                        UserSession.addoneMyCard(cardResponse);
                        ControllerManager.getDashboardHomeController().changeTab();
                        ViewManager.changeView("dashboard_home.fxml", 1);
                    }
                    else if(ResponseStatus.FAILED.equals(response.getStatus())) {
                        showError(response.getMessage());
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> showError("Không thể kết nối đến máy chủ."));
                    return null;
                });
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
        return true;
    }
    private ItemCondition getItemCondition(){
        return switch (conditionChoice.getValue()){
            case "New" ->{yield ItemCondition.NEW;}
            case "Like New"->{yield ItemCondition.LIKE_NEW;}
            case "Used" ->{yield ItemCondition.USED;}
            default -> null;
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
    private double calculateRealPrice(TextField value, ComboBox<String> Case){
        if(Case.getValue().equals("Thousand")){
            return Double.parseDouble(value.getText().trim())*1000;
        }
        if(Case.getValue().equals("Million")){
            return Double.parseDouble(value.getText().trim())*1000000;
        }
        if(Case.getValue().equals("Billion")){
            return Double.parseDouble(value.getText().trim())*1000000000;
        }
        return 0;
    }
    private long calculateStartTime() {
        LocalDate date = startDatePicker.getValue();
        String[] parts = startTimeField.getText().split(":");
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
        System.err.println("[CreateAuction] " + msg);
    }

    @FXML
    public void handleCancelButton(ActionEvent event){
        ViewManager.changeView("dashboard_home.fxml", 1);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void initialize(URL location, ResourceBundle resources){

    }
}
