package vn.io.huangnosimp.model;

import vn.io.huangnosimp.enums.ItemCondition;

public class Electronics extends Item {

    private final String brand;

    private final int warrantyMonths;

    public Electronics(String ownerId, String name, String description, String brand, int warrantyMonths, ItemCondition condition) {
        super(ownerId, name, description, condition);
        this.brand = brand;
        this.warrantyMonths = warrantyMonths;
    }

    public String getBrand() {
        return brand;
    }

    public int getWarrantyMonths() {
        return warrantyMonths;
    }

}
