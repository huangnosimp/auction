package vn.io.huangnosimp.model;

public class Vehicle extends Item {

    private final String engineType;

    private final int mileage;

    public Vehicle(String ownerId, String name, String description, String engineType, int mileage) {
        super(ownerId, name, description);
        this.engineType = engineType;
        this.mileage = mileage;
    }


    @Override
    public void printInfo() {
        System.out.println("=== Vehicle Item ===");
        System.out.println("Name          : " + getName());
        System.out.println("Description   : " + getDescription());
        System.out.println("Engine Type   : " + engineType);
        System.out.println("Mileage       : " + mileage + " km");
    }


    public String getEngineType() {
        return engineType;
    }

    public double getMileage() {
        return mileage;
    }

    @Override
    public String toString() {
        return "Vehicle{name='" + getName() + "', engine='" + engineType + "', mileage=" + mileage + "}";
    }
}
