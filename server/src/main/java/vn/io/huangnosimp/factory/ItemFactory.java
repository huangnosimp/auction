package vn.io.huangnosimp.factory;

import vn.io.huangnosimp.enums.ItemCondition;
import vn.io.huangnosimp.enums.ItemType;
import vn.io.huangnosimp.model.*;
import vn.io.huangnosimp.dto.shared.ItemAttributesDTO;

public class ItemFactory {
    public static Item createItem(String ownerID, String name, String description, ItemType type, ItemAttributesDTO attributes, ItemCondition condition, String imageUrl) {
        switch (type) {
            case ART:
                String artist = attributes.getArtist();
                int creationYear = attributes.getCreationYear();
                return new Art(ownerID, name, description, artist, creationYear, condition, imageUrl);
            case ELECTRONICS:
                String brand = attributes.getBrand();
                int warrantyMonths = attributes.getWarrantyMonths();
                return new Electronics(ownerID, name, description, brand, warrantyMonths, condition, imageUrl);
            case VEHICLE:
                String engineType = attributes.getEngineType();
                int mileage = attributes.getMileage();
                return new Vehicle(ownerID, name, description, engineType, mileage, condition, imageUrl);
            default:
                throw new IllegalArgumentException("Unknown item type: " + type);
        }
    }
}
