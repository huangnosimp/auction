package vn.io.huangnosimp.dto.request;

import vn.io.huangnosimp.dto.shared.ItemAttributesDTO;
import vn.io.huangnosimp.enums.ItemCondition;
import vn.io.huangnosimp.enums.ItemType;

import java.util.ArrayList;
import java.util.List;

public class CreateAuctionRequestDTO {
    private final String itemName;
    private final String description;
    private final ItemType itemType;
    private final ItemAttributesDTO attributes;
    private final double startPrice;
    private final long startTime;
    private final long endTime;
    private final ItemCondition condition;
    private final double minimumIncrement;
    private final double buyNowPrice;
    private final List<String> imagesBase64;

    // Constructor MỚI: Hỗ trợ đầy đủ các trường bao gồm ảnh
    public CreateAuctionRequestDTO(String itemName,
                                   String description,
                                   ItemType itemType,
                                   ItemAttributesDTO attributes,
                                   double startPrice,
                                   long startTime,
                                   long endTime,
                                   ItemCondition condition,
                                   double minimumIncrement,
                                   double buyNowPrice,
                                   List<String> imagesBase64) {
        this.itemName = itemName;
        this.description = description;
        this.itemType = itemType;
        this.attributes = attributes;
        this.startPrice = startPrice;
        this.startTime = startTime;
        this.endTime = endTime;
        this.condition = condition;
        this.minimumIncrement = minimumIncrement;
        this.buyNowPrice = buyNowPrice;
        this.imagesBase64 = imagesBase64;
    }

    // Constructor CŨ: Gọi lại constructor mới và truyền vào một danh sách rỗng
    public CreateAuctionRequestDTO(String itemName,
                                   String description,
                                   ItemType itemType,
                                   ItemAttributesDTO attributes,
                                   double startPrice,
                                   long startTime,
                                   long endTime,
                                   ItemCondition condition,
                                   double minimumIncrement,
                                   double buyNowPrice) {
        this.itemName = itemName;
        this.description = description;
        this.itemType = itemType;
        this.attributes = attributes;
        this.startPrice = startPrice;
        this.startTime = startTime;
        this.endTime = endTime;
        this.condition = condition;
        this.minimumIncrement = minimumIncrement;
        this.buyNowPrice = buyNowPrice;
        this.imagesBase64 = new ArrayList<>();
    }

    // --- Getters ---
    public String getItemName() { return itemName; }
    public String getDescription() { return description; }
    public ItemType getItemType() { return itemType; }
    public ItemAttributesDTO getAttributes() { return attributes; }
    public double getStartPrice() { return startPrice; }
    public long getStartTime() { return startTime; }
    public long getEndTime() { return endTime; }
    public ItemCondition getCondition() { return condition; }
    public double getMinimumIncrement() { return minimumIncrement; }
    public double getBuyNowPrice() { return buyNowPrice; }
    public List<String> getImagesBase64() { return imagesBase64; }
}