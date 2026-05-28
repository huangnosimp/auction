package vn.io.huangnosimp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.io.huangnosimp.dto.shared.ItemAttributesDTO;
import vn.io.huangnosimp.enums.ItemCondition;
import vn.io.huangnosimp.enums.ItemType;
import vn.io.huangnosimp.model.Art;
import vn.io.huangnosimp.model.Item;
import vn.io.huangnosimp.repository.IItemRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static vn.io.huangnosimp.TestFixtures.art;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {
    @Mock
    private IItemRepository itemRepository;

    private ItemService itemService;

    @BeforeEach
    void setUp() {
        itemService = new ItemService(itemRepository);
    }

    @Test
    void createItemBuildsExpectedTypeAndPersistsIt() {
        Item item = itemService.createItem(
                "owner-1",
                "Oil Painting",
                "Landscape",
                ItemType.ART,
                ItemAttributesDTO.createArtAttributes("Test Artist", 2024),
                ItemCondition.NEW,
                List.of("https://example.com/item.png")
        );

        assertInstanceOf(Art.class, item);
        assertEquals("owner-1", item.getOwnerId());
        assertEquals("Oil Painting", item.getName());

        ArgumentCaptor<Item> itemCaptor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository).save(itemCaptor.capture());
        assertSame(item, itemCaptor.getValue());
    }

    @Test
    void transferOwnershipRejectsInvalidInput() {
        assertFalse(itemService.transferOwnership(null, "owner-2"));
        assertFalse(itemService.transferOwnership(art("owner-1"), null));

        verify(itemRepository, never()).updateOwner(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void transferOwnershipUpdatesItemAndRepository() {
        Item item = art("owner-1");
        when(itemRepository.updateOwner(item.getId(), "owner-2")).thenReturn(true);

        boolean result = itemService.transferOwnership(item, "owner-2");

        assertTrue(result);
        assertEquals("owner-2", item.getOwnerId());
        verify(itemRepository).updateOwner(item.getId(), "owner-2");
    }

    @Test
    void getItemByIdReturnsRepositoryResult() {
        Item item = art("owner-1");
        when(itemRepository.findById("item-1")).thenReturn(item);

        assertSame(item, itemService.getItemById("item-1"));
    }
}
