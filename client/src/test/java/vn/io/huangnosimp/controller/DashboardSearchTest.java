package vn.io.huangnosimp.controller;

import org.junit.jupiter.api.Test;
import vn.io.huangnosimp.dto.response.AuctionCardDTO;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DashboardSearchTest {

    /*@Test
    void testFilterAuctions() {
        DashboardController controller = new DashboardController();

        List<AuctionCardDTO> mockList = List.of(
                new AuctionCardDTO("1", "Laptop", 100, 0, 0, 0),
                new AuctionCardDTO("2", "Mouse", 10, 0, 0, 0)
        );
        controller.setAllAuctions(mockList);

        List<AuctionCardDTO> result = controller.filterAuctions("Laptop");
        assertEquals(1, result.size());
        assertEquals("Laptop", result.get(0).getProductName());

        List<AuctionCardDTO> empty = controller.filterAuctions("Keyboard");
        assertTrue(empty.isEmpty());
    }*/
}