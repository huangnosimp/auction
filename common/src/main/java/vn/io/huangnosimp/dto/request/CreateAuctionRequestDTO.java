package vn.io.huangnosimp.dto.request;

import vn.io.huangnosimp.dto.shared.ItemAttributesDTO;
import vn.io.huangnosimp.enums.ItemCondition;
import vn.io.huangnosimp.enums.ItemType;

public class CreateAuctionRequestDTO {
    private final String itemName;
    private final String description;
    private final ItemType itemType;
    private final ItemAttributesDTO attributes;
    private final double startPrice;
    private final long startTime;
    private final long endTime;
    private final ItemCondition condition;

    public CreateAuctionRequestDTO(String itemName, String description, ItemType itemType, ItemAttributesDTO attributes, double startPrice, long startTime, long endTime, ItemCondition condition) {
        this.itemName = itemName;
        this.description = description;
        this.itemType = itemType;
        this.attributes = attributes;
        this.startPrice = startPrice;
        this.startTime = startTime;
        this.endTime = endTime;
        this.condition = condition;
    }

    public String getItemName() {
        return itemName;
    }

    public String getDescription() {
        return description;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public ItemAttributesDTO getAttributes() {
        return attributes;
    }

    public double getStartPrice() {
        return startPrice;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public ItemCondition getCondition() {
        return condition;
    }
}
