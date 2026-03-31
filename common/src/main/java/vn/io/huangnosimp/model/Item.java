package vn.io.huangnosimp.model;

public abstract class Item extends Entity {

    private String name;

    private String description;

    private double startingPrice;

    protected Item() {
        super();
    }

    protected Item(String name, String description, double startingPrice) {
        super();
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
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

    public double getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(double startingPrice) {
        this.startingPrice = startingPrice;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{name='" + name + "', startingPrice=" + startingPrice + "}";
    }
}
