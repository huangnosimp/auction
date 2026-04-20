package vn.io.huangnosimp.model;

public class Vehicle extends Item {

    private final String engineType;

    private final int mileage;

    public Vehicle(String ownerId, String name, String description, String engineType, int mileage) {
        super(ownerId, name, description);
        this.engineType = engineType;
        this.mileage = mileage;
    }

    public String getEngineType() {
        return engineType;
    }

    public double getMileage() {
        return mileage;
    }
}
