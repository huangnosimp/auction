package vn.io.huangnosimp.model;

public class Electronics extends Item {

    private final String brand;

    private final int warrantyMonths;

    public Electronics(String ownerId, String name, String description, String brand, int warrantyMonths) {
        super(ownerId, name, description);
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
