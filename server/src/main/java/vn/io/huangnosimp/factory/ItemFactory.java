package vn.io.huangnosimp.factory;

import vn.io.huangnosimp.model.*;

import java.util.HashMap;

public class ItemFactory {
    public static Item createItem(String ownerID, String name, String description, ItemType type, HashMap<String, String> attributes) {
        switch (type) {
            case ART:
                String artist = attributes.get("artist");
                int creationYear = Integer.parseInt(attributes.get("creationYear"));
                return new Art(ownerID, name, description, artist, creationYear);
            case ELECTRONICS:
                String brand = attributes.get("brand");
                int warrantyMonths = Integer.parseInt(attributes.get("warrantyMonths"));
                return new Electronics(ownerID, name, description, brand, warrantyMonths);
            case VEHICLE:
                String engineType = attributes.get("engineType");
                int mileage = Integer.parseInt(attributes.get("mileage"));
                return new Vehicle(ownerID, name, description, engineType, mileage);
            case GENERIC:
            default:
                String customCategory = attributes.get("customCategory");
                return new GenericItem(ownerID, name, description, customCategory, attributes);
        }
    }
}
