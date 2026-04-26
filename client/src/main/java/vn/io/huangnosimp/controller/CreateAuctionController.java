package vn.io.huangnosimp.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import vn.io.huangnosimp.Manager.ControllerManager;
import vn.io.huangnosimp.Manager.SocketManager;
import vn.io.huangnosimp.Manager.ViewManager;
import vn.io.huangnosimp.dto.request.CreateAuctionRequestDTO;
import vn.io.huangnosimp.dto.response.AuctionCardDTO;
import vn.io.huangnosimp.dto.response.DashboardResponseDTO;
import vn.io.huangnosimp.dto.shared.ItemAttributesDTO;
import vn.io.huangnosimp.enums.ItemType;
import vn.io.huangnosimp.protocol.ActionType;
import vn.io.huangnosimp.protocol.Request;
import vn.io.huangnosimp.protocol.ResponseStatus;
import vn.io.huangnosimp.util.GsonParser;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ResourceBundle;

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
    @FXML private Spinner<Integer> bidIncrementSpinner;
    @FXML private TextField buyNowField;
    @FXML private TextField reservePriceField;

    // Schedule
    @FXML private DatePicker startDatePicker;
    @FXML private TextField startTimeField;
    @FXML private ComboBox<String> durationCombo;
    @FXML private CheckBox autoExtendCheck;
    private DashboardController parent;
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
    private void handleLaunchButton() {
        if (!validate()) return;

        ItemAttributesDTO attributes = buildAttributes();
        if (attributes == null) return;

        long startTime = calculateStartTime();
        long endTime   = calculateEndTime(startTime);

        CreateAuctionRequestDTO dto = new CreateAuctionRequestDTO(
                itemNameField.getText().trim(),
                descriptionArea.getText().trim(),
                ItemType.valueOf(categoryCombo.getValue().toUpperCase()),
                attributes,
                Double.parseDouble(startPriceField.getText().trim()),
                startTime,
                endTime
        );

        SocketManager.getClient().sendRequestAsync(new Request(ActionType.CREATE_AUCTION, dto))
                .thenAccept(response -> Platform.runLater(() -> {
                    if (ResponseStatus.SUCCESS.equals(response.getStatus())) {
                        AuctionCardDTO cardResponse = GsonParser.GSON.fromJson(GsonParser.GSON.toJsonTree(response.getData()), AuctionCardDTO.class);
                        ControllerManager.getDashboardHomeController().addToDashboard(cardResponse);
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

    private long calculateStartTime() {
        LocalDate date = startDatePicker.getValue();
        String[] parts = startTimeField.getText().split(":");
        int hour = parts.length > 0 ? Integer.parseInt(parts[0].trim()) : 0;
        int min  = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 0;
        return LocalDateTime.of(date, LocalTime.of(hour, min))
                .toInstant(ZoneOffset.UTC).getEpochSecond();
    }

    private long calculateEndTime(long startTime) {
        long duration = switch (durationCombo.getValue()) {
            case "15 minutes" -> 900L;
            case "30 minutes" -> 1_800L;
            case "45 minutes" -> 2_700L;
            case "1 hour"   -> 3_600L;
            case "3 hours"  -> 10_800L;
            case "6 hours"  -> 21_600L;
            case "12 hours" -> 43_200L;
            case "1 day"    -> 86_400L;
            case "3 days"   -> 259_200L;
            case "7 days"   -> 604_800L;
            default         -> 86_400L;
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
