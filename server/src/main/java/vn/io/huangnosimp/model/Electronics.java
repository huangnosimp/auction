package vn.io.huangnosimp.model;

public class Electronics extends Item {

    private final String brand;

    private final int warrantyMonths;

    public Electronics(String ownerId, String name, String description, String brand, int warrantyMonths) {
        super(ownerId, name, description);
        this.brand = brand;
        this.warrantyMonths = warrantyMonths;
    }


    @Override
    public void printInfo() {
        System.out.println("=== Electronics Item ===");
        System.out.println("Name          : " + getName());
        System.out.println("Description   : " + getDescription());
        System.out.println("Brand         : " + brand);
        System.out.println("Warranty      : " + warrantyMonths + " months");
    }


    public String getBrand() {
        return brand;
    }

    public int getWarrantyMonths() {
        return warrantyMonths;
    }

    @Override
    public String toString() {
        return "Electronics{name='" + getName() + "', brand='" + brand + "'}";
    }
}
