package vn.io.huangnosimp;

import vn.io.huangnosimp.enums.AuctionStatus;
import vn.io.huangnosimp.enums.ItemCondition;
import vn.io.huangnosimp.model.Art;
import vn.io.huangnosimp.model.Auction;
import vn.io.huangnosimp.model.Item;
import vn.io.huangnosimp.model.Member;

import java.time.LocalDateTime;
import java.util.List;

public final class TestFixtures {
    private TestFixtures() {
    }

    public static Member member(String id, String username, double accountBalance, double frozenBalance) {
        return new Member(
                id,
                username,
                "hashed-password",
                username + "@example.com",
                accountBalance,
                frozenBalance,
                false,
                null,
                System.currentTimeMillis()
        );
    }

    public static Member bannedMember(String id, String username, LocalDateTime banUntil) {
        return new Member(
                id,
                username,
                "hashed-password",
                username + "@example.com",
                1000.0,
                0.0,
                true,
                banUntil,
                System.currentTimeMillis()
        );
    }

    public static Art art(String ownerId) {
        return new Art(
                ownerId,
                "Oil Painting",
                "Landscape",
                "Test Artist",
                2024,
                ItemCondition.NEW,
                List.of("https://example.com/item.png")
        );
    }

    public static Auction runningAuction(String auctionId, Item item, Member seller) {
        Auction auction = new Auction(
                item,
                seller,
                100.0,
                System.currentTimeMillis() - 1_000,
                System.currentTimeMillis() + 60_000,
                10.0,
                500.0
        );
        auction.setId(auctionId);
        auction.setStatus(AuctionStatus.RUNNING);
        return auction;
    }
}
