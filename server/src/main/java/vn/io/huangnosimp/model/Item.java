package vn.io.huangnosimp.model;

public abstract class Item extends Entity {

    private String name;
    private String ownerId;
    private String description;

    public Item(String id, long createdAt, String name, String ownerId, String description) {
        super(id, createdAt);
        this.name = name;
        this.ownerId = ownerId;
        this.description = description;
    }

    protected Item(String ownerId, String name, String description) {
        super();
        this.ownerId = ownerId;
        this.name = name;
        this.description = description;
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

}
