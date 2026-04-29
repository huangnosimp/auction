package vn.io.huangnosimp.model;

import vn.io.huangnosimp.enums.ItemCondition;

public class Vehicle extends Item {

    private final String engineType;

    private final int mileage;

    public Vehicle(String ownerId, String name, String description, String engineType, int mileage, ItemCondition condition) {
        super(ownerId, name, description, condition);
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
