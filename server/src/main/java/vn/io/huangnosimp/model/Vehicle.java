package vn.io.huangnosimp.model;

public class Vehicle extends Item {

    private String engineType;

    private double mileage;

    public Vehicle() {
        super();
    }

    public Vehicle(String name, String description, double startingPrice,
                   String engineType, double mileage) {
        super(name, description, startingPrice);
        this.engineType = engineType;
        this.mileage = mileage;
    }


    @Override
    public void printInfo() {
        System.out.println("=== Vehicle Item ===");
        System.out.println("Name          : " + getName());
        System.out.println("Description   : " + getDescription());
        System.out.println("Starting Price: $" + getStartingPrice());
        System.out.println("Engine Type   : " + engineType);
        System.out.println("Mileage       : " + mileage + " km");
    }


    public String getEngineType() {
        return engineType;
    }

    public void setEngineType(String engineType) {
        this.engineType = engineType;
    }

    public double getMileage() {
        return mileage;
    }

    public void setMileage(double mileage) {
        this.mileage = mileage;
    }

    @Override
    public String toString() {
        return "Vehicle{name='" + getName() + "', engine='" + engineType + "', mileage=" + mileage + "}";
    }
}
