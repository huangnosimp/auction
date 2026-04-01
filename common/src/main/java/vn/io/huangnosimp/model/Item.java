package vn.io.huangnosimp.model;

public abstract class Item extends Entity {

    private String name;

    private String description;

    protected Item() {
        super();
    }

    protected Item(String name, String description, double startingPrice) {
        super();
        this.name = name;
        this.description = description;
    }

    public abstract void printInfo();


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

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{name='" + name + "', description" + description + "}";
    }
}
