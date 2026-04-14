package vn.io.huangnosimp.network;

import vn.io.huangnosimp.model.ItemType;

import java.util.HashMap;

public class CreateAuctionWithSTETRequestDTO {
    private String itemName;
    private String itemDescription;
    private ItemType itemType;
    private HashMap<String, String> itemAttributes;
    private double startPrice;
    private long startTime;
    private long endTime;

    public CreateAuctionWithSTETRequestDTO(String itemName, String itemDescription, ItemType itemType, HashMap<String, String> itemAttributes, double startPrice, long startTime, long endTime) {
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.itemType = itemType;
        this.itemAttributes = itemAttributes;
        this.startPrice = startPrice;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getItemName() {
        return itemName;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public HashMap<String, String> getItemAttributes() {
        return itemAttributes;
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
}
