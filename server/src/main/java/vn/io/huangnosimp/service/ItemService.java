package vn.io.huangnosimp.service;

import vn.io.huangnosimp.factory.ItemFactory;
import vn.io.huangnosimp.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ItemService {
    private static volatile ItemService instance;
    private final ConcurrentHashMap<String, Item> itemStore;

    private ItemService() {
        itemStore = new ConcurrentHashMap<>();
    }
    public static ItemService getInstance() {
        if (instance == null) {
            synchronized (ItemService.class) {
                if (instance == null) {
                    instance = new ItemService();
                }
            }
        }
        return instance;
    }
    public Item getItem(String ownerId, String name, String description, ItemType type, HashMap<String, String> attributes) {
        Item item = ItemFactory.createItem(ownerId, name, description, type, attributes);
        itemStore.put(item.getId(), item);
        return item;
    }

    public boolean transferOwnership(Item item, String newOwnerId) {
        if (item == null || newOwnerId == null) {
            return false;
        }
        item.setOwnerId(newOwnerId);
        return true;
    }
    public ArrayList<Item> getItemsByOwner(String ownerId) {
        ArrayList<Item> items = new ArrayList<>();
        for (Item item : itemStore.values()) {
            if (item.getOwnerId().equals(ownerId)) {
                items.add(item);
            }
        }
        return items;
    }
}
