package vn.io.huangnosimp.network;

import vn.io.huangnosimp.model.ItemType;

import java.util.HashMap;

public class CreateAuctionWithDurationRequestDTO {
    private String itemName;
    private String itemDescription;
    private ItemType itemType;
    private HashMap<String, String> itemAttributes;
    private double startPrice;
    private int durationInMinutes;

    public CreateAuctionWithDurationRequestDTO(String itemName, String itemDescription, ItemType itemType, HashMap<String,String> itemAttributes, double startPrice, int durationInMinutes) {
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.itemType = itemType;
        this.itemAttributes = itemAttributes;
        this.startPrice = startPrice;
        this.durationInMinutes = durationInMinutes;
    }

    public String getItemName() {
        return itemName;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public HashMap<String, String> getItemAttributes() {
        return itemAttributes;
    }

    public int getDurationInMinutes() {
        return durationInMinutes;
    }

    public double getStartPrice() {
        return startPrice;
    }
}
