package vn.io.huangnosimp.repository;

import vn.io.huangnosimp.model.Item;

import java.util.List;

public interface IItemRepository {
    void save(Item item);
    Item findById(String itemId);
    boolean updateOwner(String itemId, String ownerId);
    boolean delete(String itemId);
}
