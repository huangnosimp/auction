package vn.io.huangnosimp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.io.huangnosimp.dto.response.AuctionActionResult;
import vn.io.huangnosimp.dto.response.AuctionCardDTO;
import vn.io.huangnosimp.dto.response.BidResult;
import vn.io.huangnosimp.dto.response.TransactionResult;
import vn.io.huangnosimp.dto.shared.ItemAttributesDTO;
import vn.io.huangnosimp.enums.AuctionStatus;
import vn.io.huangnosimp.enums.ItemCondition;
import vn.io.huangnosimp.enums.ItemType;
import vn.io.huangnosimp.model.Art;
import vn.io.huangnosimp.model.Auction;
import vn.io.huangnosimp.model.Member;
import vn.io.huangnosimp.network.ClientHandle;
import vn.io.huangnosimp.repository.IAuctionParticipantsRepository;
import vn.io.huangnosimp.repository.IAuctionRepository;
import vn.io.huangnosimp.repository.IBidTransactionRepository;
import vn.io.huangnosimp.repository.ITransactionRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static vn.io.huangnosimp.TestFixtures.art;
import static vn.io.huangnosimp.TestFixtures.member;
import static vn.io.huangnosimp.TestFixtures.runningAuction;

@ExtendWith(MockitoExtension.class)
class AuctionServiceTest {
    @Mock
    private IAuctionRepository auctionRepository;
    @Mock
    private IUserService userService;
    @Mock
    private IItemService itemService;
    @Mock
    private ITransactionRepository transactionRepository;
    @Mock
    private IAuctionParticipantsRepository auctionParticipantsRepository;
    @Mock
    private IBidTransactionRepository bidTransactionRepository;
    @Mock
    private AuctionScheduler scheduler;
    @Mock
    private IAutoBidService autoBidService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private ClientHandle client;

    private AuctionService auctionService;

    @BeforeEach
    void setUp() {
        auctionService = new AuctionService(
                auctionRepository,
                userService,
                itemService,
                transactionRepository,
                auctionParticipantsRepository,
                bidTransactionRepository
        );
    }

    @Test
    void createAuctionPersistsItemAuctionAndSchedulesIt() {
        Member seller = member("seller-1", "seller", 0.0, 0.0);
        Art item = art("seller-1");
        when(userService.getMember("seller-1")).thenReturn(seller);
        when(itemService.createItem(
                eq("seller-1"),
                eq("Oil Painting"),
                eq("Landscape"),
                eq(ItemType.ART),
                any(ItemAttributesDTO.class),
                eq(ItemCondition.NEW),
                eq(List.of("https://example.com/item.png"))
        )).thenReturn(item);
        when(auctionRepository.save(any(Auction.class))).thenReturn(true);
        auctionService.setScheduler(scheduler);

        AuctionCardDTO card = auctionService.createAuction(
                "seller-1",
                "Oil Painting",
                "Landscape",
                ItemType.ART,
                ItemAttributesDTO.createArtAttributes("Test Artist", 2024),
                100.0,
                System.currentTimeMillis() + 60_000,
                System.currentTimeMillis() + 120_000,
                ItemCondition.NEW,
                10.0,
                500.0,
                List.of("https://example.com/item.png")
        );

        assertNotNull(card);
        assertEquals("Oil Painting", card.getProductName());
        assertEquals(ItemType.ART, card.getItemType());
        assertEquals(100.0, card.getCurrentPrice());
        verify(auctionRepository).save(any(Auction.class));
        verify(scheduler).scheduleAuction(any(Auction.class));
    }

    @Test
    void placeBidRejectsNonParticipant() {
        Auction auction = runningAuction("auction-1", art("seller-1"), member("seller-1", "seller", 0.0, 0.0));
        when(auctionRepository.findById("auction-1")).thenReturn(auction);
        when(auctionParticipantsRepository.isParticipant("auction-1", "bidder-1")).thenReturn(false);

        BidResult result = auctionService.placeBid("bidder-1", "auction-1", 100.0, false);

        assertEquals(BidResult.NOT_IN_ROOM, result);
        verify(bidTransactionRepository, never()).saveBidTransaction(any());
    }

    @Test
    void placeBidRejectsAmountBelowRequiredMinimum() {
        Auction auction = runningAuction("auction-1", art("seller-1"), member("seller-1", "seller", 0.0, 0.0));
        when(auctionRepository.findById("auction-1")).thenReturn(auction);
        when(auctionParticipantsRepository.isParticipant("auction-1", "bidder-1")).thenReturn(true);
        when(userService.getMember("bidder-1")).thenReturn(member("bidder-1", "bidder", 1_000.0, 0.0));

        BidResult result = auctionService.placeBid("bidder-1", "auction-1", 99.0, false);

        assertEquals(BidResult.BID_TOO_LOW, result);
        verify(auctionRepository, never()).save(any());
        verify(bidTransactionRepository, never()).saveBidTransaction(any());
    }

    @Test
    void placeBidFreezesBidderMoneyAndSavesBid() {
        Member bidder = member("bidder-1", "bidder", 1_000.0, 0.0);
        Auction auction = runningAuction("auction-1", art("seller-1"), member("seller-1", "seller", 0.0, 0.0));
        when(auctionRepository.findById("auction-1")).thenReturn(auction);
        when(auctionParticipantsRepository.isParticipant("auction-1", "bidder-1")).thenReturn(true);
        when(userService.getMember("bidder-1")).thenReturn(bidder);
        when(userService.updateBalance(anyString(), anyDouble())).thenReturn(TransactionResult.SUCCESS);
        when(userService.updateFrozenBalance(anyString(), anyDouble())).thenReturn(TransactionResult.SUCCESS);
        when(auctionRepository.save(any(Auction.class))).thenReturn(true);
        when(bidTransactionRepository.saveBidTransaction(any())).thenReturn(true);

        BidResult result = auctionService.placeBid("bidder-1", "auction-1", 150.0, false);

        assertEquals(BidResult.SUCCESS, result);
        assertEquals(850.0, bidder.getAccountBalance());
        assertEquals(150.0, bidder.getFrozenBalance());
        assertEquals("bidder-1", auction.getCurrentWinnerId());
        assertEquals(150.0, auction.getCurrentPrice());
        verify(userService).updateBalance("bidder-1", 850.0);
        verify(userService).updateFrozenBalance("bidder-1", 150.0);
        verify(auctionRepository).save(auction);
        verify(bidTransactionRepository).saveBidTransaction(any());
    }

    @Test
    void placeBidOutbidsPreviousWinnerAndTriggersAutoBid() {
        Member previousWinner = member("winner-1", "winner", 50.0, 150.0);
        Member bidder = member("bidder-1", "bidder", 1_000.0, 0.0);
        Auction auction = runningAuction("auction-1", art("seller-1"), member("seller-1", "seller", 0.0, 0.0));
        auction.setCurrentWinnerId("winner-1");
        auction.setCurrentPrice(150.0);
        when(auctionRepository.findById("auction-1")).thenReturn(auction);
        when(auctionParticipantsRepository.isParticipant("auction-1", "bidder-1")).thenReturn(true);
        when(userService.getMember("bidder-1")).thenReturn(bidder);
        when(userService.getMember("winner-1")).thenReturn(previousWinner);
        when(userService.updateBalance(anyString(), anyDouble())).thenReturn(TransactionResult.SUCCESS);
        when(userService.updateFrozenBalance(anyString(), anyDouble())).thenReturn(TransactionResult.SUCCESS);
        when(auctionRepository.save(any(Auction.class))).thenReturn(true);
        when(bidTransactionRepository.saveBidTransaction(any())).thenReturn(true);
        auctionService.setAutoBidService(autoBidService);

        BidResult result = auctionService.placeBid("bidder-1", "auction-1", 200.0, true);

        assertEquals(BidResult.SUCCESS, result);
        assertEquals(200.0, previousWinner.getAccountBalance());
        assertEquals(0.0, previousWinner.getFrozenBalance());
        assertEquals(800.0, bidder.getAccountBalance());
        assertEquals(200.0, bidder.getFrozenBalance());
        assertEquals("bidder-1", auction.getCurrentWinnerId());
        verify(autoBidService).processAutoBids("auction-1");
    }

    @Test
    void placeBidInFinalSecondsExtendsAuctionAndNotifiesClients() {
        Member bidder = member("bidder-1", "bidder", 1_000.0, 0.0);
        Auction auction = new Auction(
                art("seller-1"),
                member("seller-1", "seller", 0.0, 0.0),
                100.0,
                System.currentTimeMillis() - 1_000,
                System.currentTimeMillis() + 5_000,
                10.0,
                500.0
        );
        auction.setId("auction-1");
        auction.setStatus(AuctionStatus.RUNNING);
        when(auctionRepository.findById("auction-1")).thenReturn(auction);
        when(auctionParticipantsRepository.isParticipant("auction-1", "bidder-1")).thenReturn(true);
        when(userService.getMember("bidder-1")).thenReturn(bidder);
        when(userService.updateBalance(anyString(), anyDouble())).thenReturn(TransactionResult.SUCCESS);
        when(userService.updateFrozenBalance(anyString(), anyDouble())).thenReturn(TransactionResult.SUCCESS);
        when(auctionRepository.save(any(Auction.class))).thenReturn(true);
        when(bidTransactionRepository.saveBidTransaction(any())).thenReturn(true);
        doAnswer(invocation -> {
            Auction extendedAuction = invocation.getArgument(0);
            long newEndTime = invocation.getArgument(1);
            return extendedAuction.extendEndTime(newEndTime);
        }).when(scheduler).extendTime(eq(auction), anyLong());
        auctionService.setScheduler(scheduler);
        auctionService.setNotificationService(notificationService);

        BidResult result = auctionService.placeBid("bidder-1", "auction-1", 150.0, false);

        assertEquals(BidResult.SUCCESS, result);
        verify(scheduler).extendTime(eq(auction), anyLong());
        verify(notificationService).notifyAuctionExtended("auction-1", auction.getEndTime());
    }

    @Test
    void buyNowTransfersOwnershipPaysSellerAndMarksAuctionPaid() {
        Member seller = member("seller-1", "seller", 0.0, 0.0);
        Member buyer = member("buyer-1", "buyer", 1_000.0, 0.0);
        Auction auction = runningAuction("auction-1", art("seller-1"), seller);
        when(auctionRepository.findById("auction-1")).thenReturn(auction);
        when(userService.getMember("buyer-1")).thenReturn(buyer);
        when(itemService.transferOwnership(auction.getItem(), "buyer-1")).thenReturn(true);
        when(userService.updateBalance(anyString(), anyDouble())).thenReturn(TransactionResult.SUCCESS);
        when(userService.updateFrozenBalance(anyString(), anyDouble())).thenReturn(TransactionResult.SUCCESS);
        when(auctionRepository.save(any(Auction.class))).thenReturn(true);
        when(transactionRepository.saveTransaction(any())).thenReturn(true);
        auctionService.setScheduler(scheduler);

        BidResult result = auctionService.buyNow("buyer-1", "auction-1");

        assertEquals(BidResult.SUCCESS, result);
        assertEquals(500.0, buyer.getAccountBalance());
        assertEquals(0.0, buyer.getFrozenBalance());
        assertEquals(500.0, seller.getAccountBalance());
        assertEquals(AuctionStatus.PAID, auction.getStatus());
        verify(auctionRepository).save(auction);
        verify(transactionRepository, times(2)).saveTransaction(any());
        verify(scheduler).cancelTimers("auction-1");
    }

    @Test
    void buyNowRejectsPriceBelowCurrentBid() {
        Member seller = member("seller-1", "seller", 0.0, 0.0);
        Member buyer = member("buyer-1", "buyer", 1_000.0, 0.0);
        Auction auction = runningAuction("auction-1", art("seller-1"), seller);
        auction.setCurrentWinnerId("winner-1");
        auction.setCurrentPrice(600.0);
        when(auctionRepository.findById("auction-1")).thenReturn(auction);
        when(userService.getMember("buyer-1")).thenReturn(buyer);

        BidResult result = auctionService.buyNow("buyer-1", "auction-1");

        assertEquals(BidResult.BID_TOO_LOW, result);
        verify(itemService, never()).transferOwnership(any(), anyString());
        verify(auctionRepository, never()).save(any());
    }

    @Test
    void cancelAuctionUnfreezesCurrentWinnerAndCancelsTimers() {
        Member winner = member("winner-1", "winner", 50.0, 150.0);
        Auction auction = runningAuction("auction-1", art("seller-1"), member("seller-1", "seller", 0.0, 0.0));
        auction.setCurrentWinnerId("winner-1");
        auction.setCurrentPrice(150.0);
        when(auctionRepository.findById("auction-1")).thenReturn(auction);
        when(userService.getMember("winner-1")).thenReturn(winner);
        when(userService.updateBalance(anyString(), anyDouble())).thenReturn(TransactionResult.SUCCESS);
        when(userService.updateFrozenBalance(anyString(), anyDouble())).thenReturn(TransactionResult.SUCCESS);
        when(auctionRepository.save(any(Auction.class))).thenReturn(true);
        auctionService.setScheduler(scheduler);

        AuctionActionResult result = auctionService.cancelAuction("seller-1", "auction-1");

        assertEquals(AuctionActionResult.SUCCESS, result);
        assertEquals(200.0, winner.getAccountBalance());
        assertEquals(0.0, winner.getFrozenBalance());
        assertEquals(AuctionStatus.CANCELED, auction.getStatus());
        verify(scheduler).cancelTimers("auction-1");
        verify(auctionRepository).save(auction);
    }

    @Test
    void cancelAuctionRejectsNonSeller() {
        Auction auction = runningAuction("auction-1", art("seller-1"), member("seller-1", "seller", 0.0, 0.0));
        when(auctionRepository.findById("auction-1")).thenReturn(auction);

        AuctionActionResult result = auctionService.cancelAuction("bidder-1", "auction-1");

        assertEquals(AuctionActionResult.UNAUTHORIZED, result);
        verify(auctionRepository, never()).save(any());
    }

    @Test
    void joinAndLeaveAuctionUpdateParticipants() {
        Auction auction = runningAuction("auction-1", art("seller-1"), member("seller-1", "seller", 0.0, 0.0));
        Member bidder = member("bidder-1", "bidder", 1_000.0, 0.0);
        when(auctionRepository.findById("auction-1")).thenReturn(auction);
        when(userService.getMember("bidder-1")).thenReturn(bidder);

        assertEquals(AuctionActionResult.SUCCESS, auctionService.joinAuction("bidder-1", "auction-1", client));
        assertEquals(AuctionActionResult.SUCCESS, auctionService.leaveAuction("bidder-1", "auction-1", client));

        verify(auctionParticipantsRepository).addParticipant("auction-1", "bidder-1");
        verify(auctionParticipantsRepository).removeParticipant("auction-1", "bidder-1");
    }
}
