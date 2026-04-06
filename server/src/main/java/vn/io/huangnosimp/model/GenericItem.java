package vn.io.huangnosimp.model;

import java.util.HashMap;
import java.util.Map;

public class GenericItem extends Item {

    private String customCategory;

    private Map<String, String> dynamicAttributes;

    public GenericItem() {
        super();
        this.dynamicAttributes = new HashMap<>();
    }

    public GenericItem(String name, String description, double startingPrice,
                       String customCategory) {
        super(name, description, startingPrice);
        this.customCategory = customCategory;
        this.dynamicAttributes = new HashMap<>();
    }

    public GenericItem(String name, String description, double startingPrice,
                       String customCategory, Map<String, String> dynamicAttributes) {
        super(name, description, startingPrice);
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

    public void setCustomCategory(String customCategory) {
        this.customCategory = customCategory;
    }

    public Map<String, String> getDynamicAttributes() {
        return dynamicAttributes;
    }

    public void setDynamicAttributes(Map<String, String> dynamicAttributes) {
        this.dynamicAttributes = dynamicAttributes;
    }

    public void putAttribute(String key, String value) {
        this.dynamicAttributes.put(key, value);
    }

    public String getAttribute(String key) {
        return this.dynamicAttributes.get(key);
    }

    @Override
    public String toString() {
        return "GenericItem{name='" + getName() + "', category='" + customCategory + "'}";
    }
}
