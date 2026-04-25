package vn.io.huangnosimp.service;

import vn.io.huangnosimp.model.Item;
import vn.io.huangnosimp.enums.ItemType;
import vn.io.huangnosimp.dto.shared.ItemAttributesDTO;

import java.util.List;

public interface IItemService {
    Item createItem(String ownerId, String name, String description, ItemType type, ItemAttributesDTO attributes);
    Item getItemById(String id);
    boolean transferOwnership(Item item, String newOwnerId);
}
