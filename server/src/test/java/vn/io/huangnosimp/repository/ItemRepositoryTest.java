package vn.io.huangnosimp.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.io.huangnosimp.enums.ItemCondition;
import vn.io.huangnosimp.model.Art;
import vn.io.huangnosimp.model.Electronics;
import vn.io.huangnosimp.model.Item;
import vn.io.huangnosimp.model.Vehicle;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemRepositoryTest extends H2RepositoryTestSupport {
    private ItemRepository itemRepository;

    @BeforeEach
    void setUpRepository() {
        itemRepository = new ItemRepository(databaseConnection);
    }

    @Test
    void saveAndFindArtWithImages() throws SQLException {
        Art item = new Art(
                "owner-1",
                "Oil Painting",
                "Landscape",
                "Test Artist",
                2024,
                ItemCondition.NEW,
                List.of("https://example.com/one.png", "https://example.com/two.png")
        );

        itemRepository.save(item);
        Item found = itemRepository.findById(item.getId());

        Art art = assertInstanceOf(Art.class, found);
        assertEquals("owner-1", art.getOwnerId());
        assertEquals("Oil Painting", art.getName());
        assertEquals("Test Artist", art.getArtist());
        assertEquals(2024, art.getCreationYear());
        assertEquals(List.of("https://example.com/one.png", "https://example.com/two.png"), art.getImageUrl());
        assertEquals(2, countRows("ItemImages"));
    }

    @Test
    void saveAndFindElectronicsAndVehicle() {
        Electronics electronics = new Electronics(
                "owner-1",
                "Phone",
                "Flagship",
                "ACME",
                24,
                ItemCondition.USED,
                List.of()
        );
        Vehicle vehicle = new Vehicle(
                "owner-2",
                "Motorbike",
                "City bike",
                "Gasoline",
                1_500,
                ItemCondition.NEW,
                List.of()
        );

        itemRepository.save(electronics);
        itemRepository.save(vehicle);

        Electronics foundElectronics = assertInstanceOf(Electronics.class, itemRepository.findById(electronics.getId()));
        Vehicle foundVehicle = assertInstanceOf(Vehicle.class, itemRepository.findById(vehicle.getId()));
        assertEquals("ACME", foundElectronics.getBrand());
        assertEquals(24, foundElectronics.getWarrantyMonths());
        assertEquals("Gasoline", foundVehicle.getEngineType());
        assertEquals(1_500.0, foundVehicle.getMileage());
    }

    @Test
    void updateOwnerAndDelete() {
        Art item = new Art("owner-1", "Oil Painting", "Landscape", "Test Artist", 2024, ItemCondition.NEW, List.of());
        itemRepository.save(item);

        itemRepository.updateOwner(item.getId(), "owner-2");
        assertEquals("owner-2", itemRepository.findById(item.getId()).getOwnerId());

        assertTrue(itemRepository.delete(item.getId()));
        assertNull(itemRepository.findById(item.getId()));
    }
}
