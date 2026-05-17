package vn.io.huangnosimp.model;

import vn.io.huangnosimp.enums.ItemCondition;

import java.util.List;

public abstract class Item extends Entity {

    private String name;
    private String ownerId;
    private String description;
    private ItemCondition condition;
    private List<String> imageUrl;

    public Item(String id, long createdAt, String name, String ownerId, String description, ItemCondition condition, List<String> imageUrl) {
        super(id, createdAt);
        this.name = name;
        this.ownerId = ownerId;
        this.description = description;
        this.condition = condition;
        this.imageUrl = imageUrl;
    }

    protected Item(String ownerId, String name, String description, ItemCondition condition, List<String> imageUrl) {
        super();
        this.ownerId = ownerId;
        this.name = name;
        this.description = description;
        this.condition = condition;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public ItemCondition getCondition() {
        return condition;
    }

    public void setCondition(ItemCondition condition) {
        this.condition = condition;
    }

    public List<String> getImageUrl() {
        return imageUrl;
    }
}
