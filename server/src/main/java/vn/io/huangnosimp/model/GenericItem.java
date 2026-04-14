package vn.io.huangnosimp.model;

import java.util.HashMap;

public class GenericItem extends Item {

    private final String customCategory;

    private final HashMap<String, String> dynamicAttributes;

    public GenericItem(String ownerId, String name, String description, String customCategory, HashMap<String, String> dynamicAttributes) {
        super(ownerId, name, description);
        this.customCategory = customCategory;
        this.dynamicAttributes = dynamicAttributes != null ? dynamicAttributes : new HashMap<>();
    }


    @Override
    public void printInfo() {
        System.out.println("=== Generic Item ===");
        System.out.println("Name           : " + getName());
        System.out.println("Description    : " + getDescription());
        System.out.println("Custom Category: " + customCategory);
        if (dynamicAttributes != null && !dynamicAttributes.isEmpty()) {
            System.out.println("Dynamic Attributes:");
            dynamicAttributes.forEach((key, value) ->
                    System.out.println("  " + key + " : " + value));
        }
    }

    public String getCustomCategory() {
        return customCategory;
    }

    public HashMap<String, String> getDynamicAttributes() {
        return dynamicAttributes;
    }

    @Override
    public String toString() {
        return "GenericItem{name='" + getName() + "', category='" + customCategory + "'}";
    }
}
