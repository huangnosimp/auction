package vn.io.huangnosimp.model;

import vn.io.huangnosimp.enums.ItemCondition;

import java.util.List;

public class Electronics extends Item {

    private final String brand;

    private final int warrantyMonths;

    public Electronics(String ownerId, String name, String description, String brand, int warrantyMonths, ItemCondition condition, List<String> imageUrl) {
        super(ownerId, name, description, condition,  imageUrl);
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
