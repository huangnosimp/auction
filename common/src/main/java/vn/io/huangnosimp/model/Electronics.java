package vn.io.huangnosimp.model;

public class Electronics extends Item {

    private String brand;

    private int warrantyMonths;

    public Electronics() {
        super();
    }

    public Electronics(String name, String description, double startingPrice,
                       String brand, int warrantyMonths) {
        super(name, description, startingPrice);
        this.brand = brand;
        this.warrantyMonths = warrantyMonths;
    }


    @Override
    public void printInfo() {
        System.out.println("=== Electronics Item ===");
        System.out.println("Name          : " + getName());
        System.out.println("Description   : " + getDescription());
        System.out.println("Starting Price: $" + getStartingPrice());
        System.out.println("Brand         : " + brand);
        System.out.println("Warranty      : " + warrantyMonths + " months");
    }


    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public int getWarrantyMonths() {
        return warrantyMonths;
    }

    public void setWarrantyMonths(int warrantyMonths) {
        this.warrantyMonths = warrantyMonths;
    }

    @Override
    public String toString() {
        return "Electronics{name='" + getName() + "', brand='" + brand + "'}";
    }
}
