package vn.io.huangnosimp.unit.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import vn.io.huangnosimp.model.*;
import vn.io.huangnosimp.repository.IAuctionRepository;
import vn.io.huangnosimp.repository.ITransactionRepository;
import vn.io.huangnosimp.service.*;

import java.util.concurrent.ConcurrentHashMap;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuctionTest {

    private IAuctionRepository auctionRepo;
    private IUserService userService;
    private IItemService itemService;
    private ITransactionRepository transRepo;
    private NotificationService notificationService;

    private AuctionService auctionService;

    private Item item;
    private Auction auction;
    private Member seller;
    private Member bidderA;
    private Member bidderB;
    private ConcurrentHashMap<String, Member> bidders;

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

        seller = mock(Member.class);
        when(seller.getId()).thenReturn("SELLER_ID");

        item = new Item("ITEM_ID", "Test Item", "A test item for auction") {};

        bidderA = new Member("A", "BidderA", "passA", "a@testmail.com", 1000.0, 500.0, false);
        when(userService.getMember("A")).thenReturn(bidderA);

        bidderB = new Member("B", "BidderB", "passB", "b@testmail.com", 1000.0, 500.0, false);
        when(userService.getMember("B")).thenReturn(bidderB);

        bidders = new ConcurrentHashMap<>();
        bidders.put(bidderA.getId(), bidderA);
        bidders.put(bidderB.getId(), bidderB);

        long startTime = System.currentTimeMillis();
        long endTime = System.currentTimeMillis() + 100000;

        auction = new Auction("A1", item, seller, 100.0, startTime, endTime, bidders);
        when(auctionRepo.findById("A1")).thenReturn(auction);

        auction.setStatusRunning();
    }

    @Test
    @DisplayName("Giá đặt thấp hơn giá hiện tại phải bị từ chối")
    void placeBid_LowerPrice_ShouldReturnFalse() {
        auction.setCurrentPrice(100.0);
        boolean result = auctionService.placeBid("A", "A1", 50.0, false);
        assertFalse(result, "Hệ thống phải từ chối giá thấp hơn giá hiện tại");
    }

    @Test
    @DisplayName("Người thắng cũ phải được hoàn tiền khi có người mới đặt giá cao hơn")
    void placeBid_NewWinner_ShouldRefundPreviousWinner() {
        auction.setCurrentWinnerId("A");
        auction.setCurrentPrice(150.0);

        boolean result = auctionService.placeBid("B", "A1", 200.0, false);
        assertTrue(result);
        assertEquals("B", auction.getCurrentWinnerId());
        assertEquals(200.0, auction.getCurrentPrice());
        assertEquals(0.0, bidderA.getFrozenBalance(), "Người thắng cũ phải được hoàn tiền (unfreeze)");
    }

    @Test
    @DisplayName("Người đang thắng tự nâng giá thì chỉ đóng băng thêm phần chênh lệch")
    void placeBid_SameWinnerRaisesPrice_ShouldOnlyFreezeDelta() {
        auction.setCurrentWinnerId("A");
        auction.setCurrentPrice(150.0);

        boolean result = auctionService.placeBid("A", "A1", 200.0, false);

        assertTrue(result);
        assertEquals(200.0, auction.getCurrentPrice());
        assertEquals(550.0, bidderA.getFrozenBalance(), "Chỉ phần chênh lệch mới được đóng băng thêm");
    }

//    @Test
//    @DisplayName("Đặt giá khi phiên đấu giá đã kết thúc phải trả về false")
//    void placeBid_AfterEndTime_ShouldReturnFalse() {
//        long now = System.currentTimeMillis();
//        Auction expiredAuction = new Auction("A2", item, seller, 100.0, now, now + 50, bidders);
//        when(auctionRepo.findById("A2")).thenReturn(expiredAuction);
//        expiredAuction.setStatusRunning();
//
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//
//        boolean result = auctionService.placeBid("A", "A2", 150.0, false);
//        assertFalse(result, "Hệ thống phải chặn đặt giá khi đã quá endTime");
//    }

    @Test
    @DisplayName("Nếu không đủ số dư để đóng băng tiền, việc đặt giá phải thất bại")
    void placeBid_InsufficientBalance_ShouldReturnFalse() {
        auction.setCurrentWinnerId("B");
        auction.setCurrentPrice(150.0);
        boolean result = auctionService.placeBid("A", "A1", 2000.0, false);

        assertFalse(result, "Phải thất bại khi người dùng không đủ tiền");
        assertNotEquals("A", auction.getCurrentWinnerId(), "Winner không được phép cập nhật");
    }
}