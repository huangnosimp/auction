package vn.io.huangnosimp.dto.shared;

public class ItemAttributesDTO {
    //ART
    private String artist;
    private int creationYear;
    //ELECTRONICS
    private String brand;
    private int warrantyMonths;
    //VEHICLE
    private String engineType;
    private int mileage;

    public String getArtist() {
        return artist;
    }

    public int getCreationYear() {
        return creationYear;
    }

    public String getBrand() {
        return brand;
    }

    public int getWarrantyMonths() {
        return warrantyMonths;
    }

    public String getEngineType() {
        return engineType;
    }

    public int getMileage() {
        return mileage;
    }
    public static ItemAttributesDTO createArtAttributes(String artist, int creationYear) {
        ItemAttributesDTO attributes = new ItemAttributesDTO();
        attributes.artist = artist;
        attributes.creationYear = creationYear;
        return attributes;
    }
    public static ItemAttributesDTO createElectronicsAttributes(String brand, int warrantyMonths) {
        ItemAttributesDTO attributes = new ItemAttributesDTO();
        attributes.brand = brand;
        attributes.warrantyMonths = warrantyMonths;
        return attributes;
    }
    public static ItemAttributesDTO createVehicleAttributes(String engineType, int mileage) {
        ItemAttributesDTO attributes = new ItemAttributesDTO();
        attributes.engineType = engineType;
        attributes.mileage = mileage;
        return attributes;
    }
}
