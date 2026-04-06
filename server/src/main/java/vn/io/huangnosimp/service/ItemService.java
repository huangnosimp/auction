package vn.io.huangnosimp.service;

import vn.io.huangnosimp.model.*;
//prototype
public class ItemService {
    private static volatile ItemService instance;

    private ItemService() {

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
    public Item getItem(String itemId) {
        return new Art(itemId, "Art Piece " + itemId, 2020, "Artist", 2020);
    }
}
