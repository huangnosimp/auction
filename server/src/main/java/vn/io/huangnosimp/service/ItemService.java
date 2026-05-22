package vn.io.huangnosimp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vn.io.huangnosimp.enums.ItemCondition;
import vn.io.huangnosimp.enums.ItemType;
import vn.io.huangnosimp.factory.ItemFactory;
import vn.io.huangnosimp.model.*;
import vn.io.huangnosimp.dto.shared.ItemAttributesDTO;
import vn.io.huangnosimp.repository.IItemRepository;

import java.util.List;

public class ItemService implements IItemService {
    private static final Logger logger = LoggerFactory.getLogger(ItemService.class);
    private final IItemRepository itemRepository;

    public ItemService(IItemRepository iItemRepository) {
        this.itemRepository = iItemRepository;
    }

    @Override
    public Item createItem(String ownerId, String name, String description, ItemType type, ItemAttributesDTO attributes, ItemCondition condition, List<String> imageUrl) {
        Item item = ItemFactory.createItem(ownerId, name, description, type, attributes, condition, imageUrl);
        itemRepository.save(item);
        logger.info("Item created itemId={} ownerId={} type={}", item.getId(), ownerId, type);
        return item;
    }

    @Override
    public Item getItemById(String id) {
        Item item = itemRepository.findById(id);
        if (item == null) {
            logger.debug("Item lookup returned no item itemId={}", id);
        }
        return item;
    }

    @Override
    public boolean transferOwnership(Item item, String newOwnerId) {
        if (item == null || newOwnerId == null) {
            logger.warn("Item ownership transfer rejected itemPresent={} newOwnerIdPresent={}",
                    item != null, newOwnerId != null);
            return false;
        }
        item.setOwnerId(newOwnerId);
        itemRepository.updateOwner(item.getId(), newOwnerId);
        logger.info("Item ownership transferred itemId={} newOwnerId={}", item.getId(), newOwnerId);
        return true;
    }

}
