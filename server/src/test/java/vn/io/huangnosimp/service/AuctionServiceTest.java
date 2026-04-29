package vn.io.huangnosimp.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.io.huangnosimp.dto.response.AuctionResponseDTO;
import vn.io.huangnosimp.dto.shared.ItemAttributesDTO;
import vn.io.huangnosimp.enums.ItemType;
import vn.io.huangnosimp.model.Auction;
import vn.io.huangnosimp.model.Item;
import vn.io.huangnosimp.model.Member;
import vn.io.huangnosimp.network.ClientHandle;
import vn.io.huangnosimp.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

class AuctionServiceTest {
    private static final Logger log = LoggerFactory.getLogger(AuctionServiceTest.class);
    private IAuctionRepository auctionRepo;
    private IUserService userService;
    private IItemService itemService;
    private ITransactionRepository transRepo;
    private NotificationService notificationService;

    private AuctionService auctionService;

    private Auction mockAuction;
    private Item item;
    private Member bidder;
    private Member seller;
    ConcurrentHashMap<String, Member> bidders = new ConcurrentHashMap<>();

    @BeforeEach
    void setUp() {
        auctionRepo = mock(IAuctionRepository.class);
        userService = mock(IUserService.class);
        itemService = mock(IItemService.class);
        transRepo = mock(ITransactionRepository.class);
        notificationService = mock(NotificationService.class);

        auctionService = new AuctionService(auctionRepo, userService, itemService, transRepo);
        auctionService.setNotificationService(notificationService);
        auctionService.setScheduler(mock(AuctionScheduler.class));

        seller = new Member("S1", "seller1", "pass", "seller@test.com", 0.0, 0.0, false);
        bidder = new Member("B1", "bidder", "pass", "bidder@test.com", 5000.0, 0.0, false);
        item = new Item("S1", "Test Item", "A test item for auction") {};

        bidders.put(bidder.getId(), bidder);
        mockAuction = new Auction("A1", item, seller, 100.0, 0, System.currentTimeMillis() + 100000, bidders);
        mockAuction.setStatusRunning();
        when(auctionRepo.findById("A1")).thenReturn(mockAuction);
        when(userService.getMember("B1")).thenReturn(bidder);
    }

    @Test
    void testBiddingWithoutDatabase() throws InterruptedException {
        int numThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numThreads);

        for (int i = 0; i < numThreads; i++) {
            final double bidAmount = 200.0 + i;
            executor.execute(() -> {
                try {
                    startLatch.await();
                    auctionService.placeBid("B1", "A1", bidAmount, false);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean finished = doneLatch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(finished, "Test bị quá thời gian cho phép!");
        assertEquals(209.0, mockAuction.getCurrentPrice(), "Giá đấu giá không được cập nhật đúng mức cao nhất!");
        assertEquals("B1", mockAuction.getCurrentWinnerId(), "Winner ID không khớp!");
        verify(auctionRepo, atLeastOnce()).save(any(Auction.class));
        System.out.println(">>> Test thành công! Giá cuối: " + mockAuction.getCurrentPrice());
    }

    @Test
    void placeBid_ShouldReturnFalse_WhenAmountIsLowerThanCurrentPrice() {
        mockAuction.setCurrentPrice(200.0);
        when(userService.getMember("B1")).thenReturn(bidder);

        boolean result = auctionService.placeBid("B1", "A1",190.0, false);
        assertFalse(result);
        assertEquals(200.0, mockAuction.getCurrentPrice());
    }

    @Test
    void placeBid_ShouldReturnFalse_WhenBidderIsSeller() {
        boolean result = auctionService.placeBid("S1", "A1", 500.0, false);
        assertFalse(result, "Người bán không được phép tự đấu giá đồ của mình!");
    }

    @Test
    void createAuction_WhenAuctionHasExpired() {
        ItemType itemType = ItemType.ELECTRONICS;
        ItemAttributesDTO attributes = new ItemAttributesDTO();
        AuctionResponseDTO auctionResponseDTO = auctionService.createAuction(
                "S1",
                "Test Item",
                "A test item for auction",
                itemType,
                attributes,
                100.0,
                System.currentTimeMillis() - 200000,
                System.currentTimeMillis() - 100000);
        assertNull(auctionResponseDTO, "Không được phép tạo phiên đấu giá đã kết thúc!");
    }

     @Test
    void placeBid_ShouldReturnTrue_WhenBidIsValid() {
         when(userService.getMember("B1")).thenReturn(bidder);

         boolean result = auctionService.placeBid("B1", "A1", 150.0, false);
         assertTrue(result, "Đặt giá hợp lệ nên phải trả về true!");
         assertEquals(150.0, mockAuction.getCurrentPrice(), "Giá đấu giá phải được cập nhật lên mức mới!");
         assertEquals("B1", mockAuction.getCurrentWinnerId(), "Winner ID phải được cập nhật thành B1!");
         verify(auctionRepo).save(mockAuction);
     }

    @Test
    void placeBid_ShouldUnfreezePreviousWinnerMoney_WhenOutbid() {
        Member bidder2 = new Member("B2", "userA", "pass", "a@test.com", 1000.0, 150.0, false);
        bidders.put(bidder2.getId(), bidder2);
        mockAuction = new Auction("A1", item, seller, 100.0, 0, System.currentTimeMillis() + 100000, bidders);
        mockAuction.setCurrentWinnerId("A");
        mockAuction.setCurrentPrice(150.0);
        mockAuction.getBidders().put("B1", bidder);
        mockAuction.setStatusRunning();

        when(auctionRepo.findById("A1")).thenReturn(mockAuction);
        when(userService.getMember("B1")).thenReturn(bidder);
        when(userService.getMember("B2")).thenReturn(bidder2);


        System.out.println(auctionService.placeBid("B1", "A1", 200.0, false));
        assertEquals(1150.0, bidder2.getAccountBalance(), "Người A phải được hoàn lại 150 vào tài khoản");
        assertEquals(0.0, bidder2.getFrozenBalance(), "Frozen Balance của người A phải về 0");
    }
}