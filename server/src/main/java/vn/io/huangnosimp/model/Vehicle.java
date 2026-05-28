package vn.io.huangnosimp.model;

import vn.io.huangnosimp.enums.ItemCondition;

import java.util.List;

public class Vehicle extends Item {

    private final String engineType;

    private final int mileage;

    public Vehicle(String ownerId, String name, String description, String engineType, int mileage, ItemCondition condition, List<String> imageUrl) {
        super(ownerId, name, description, condition, imageUrl);
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
