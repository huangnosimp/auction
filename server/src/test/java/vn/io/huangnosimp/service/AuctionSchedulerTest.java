package vn.io.huangnosimp.service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import vn.io.huangnosimp.enums.AuctionStatus;
import vn.io.huangnosimp.model.Auction;
import vn.io.huangnosimp.repository.IAuctionParticipantsRepository;
import vn.io.huangnosimp.repository.IAuctionRepository;
import vn.io.huangnosimp.repository.IBidTransactionRepository;
import vn.io.huangnosimp.repository.ITransactionRepository;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static vn.io.huangnosimp.TestFixtures.art;
import static vn.io.huangnosimp.TestFixtures.member;

class AuctionSchedulerTest {
    @AfterAll
    static void tearDownScheduler() {
        AuctionScheduler.shutdown();
    }

    @Test
    void scheduleAuctionStartsAndFinishesAuctionAtConfiguredTimes() {
        IAuctionRepository auctionRepository = mock(IAuctionRepository.class);
        IUserService userService = mock(IUserService.class);
        IItemService itemService = mock(IItemService.class);
        ITransactionRepository transactionRepository = mock(ITransactionRepository.class);
        IAuctionParticipantsRepository participantsRepository = mock(IAuctionParticipantsRepository.class);
        IBidTransactionRepository bidTransactionRepository = mock(IBidTransactionRepository.class);
        AuctionService auctionService = new AuctionService(
                auctionRepository,
                userService,
                itemService,
                transactionRepository,
                participantsRepository,
                bidTransactionRepository
        );
        Auction auction = new Auction(
                art("seller-1"),
                member("seller-1", "seller", 0.0, 0.0),
                100.0,
                System.currentTimeMillis() + 30,
                System.currentTimeMillis() + 120,
                10.0,
                500.0
        );
        auction.setId("auction-1");
        auction.setStatus(AuctionStatus.OPEN);
        when(auctionRepository.findById("auction-1")).thenReturn(auction);

        new AuctionScheduler(auctionService).scheduleAuction(auction);

        await().atMost(Duration.ofSeconds(2)).untilAsserted(() ->
                assertEquals(AuctionStatus.FINISHED, auction.getStatus()));
        verify(auctionRepository, atLeast(2)).save(auction);
    }
}
