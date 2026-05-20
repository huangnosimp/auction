package vn.io.huangnosimp.repository;

import vn.io.huangnosimp.connection.DatabaseConnection;
import vn.io.huangnosimp.model.Auction;
import vn.io.huangnosimp.enums.AuctionStatus;
import vn.io.huangnosimp.model.Item;
import vn.io.huangnosimp.model.Member;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuctionRepository implements IAuctionRepository {
    private static final Logger logger = LoggerFactory.getLogger(AuctionRepository.class);
    private final DatabaseConnection databaseConnection;
    private final IUserRepository userRepository;
    private final IItemRepository itemRepository;

    public AuctionRepository(DatabaseConnection databaseConnection, IUserRepository userRepository, IItemRepository itemRepository) {
        this.databaseConnection = databaseConnection;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public void save(Auction auction) {
        if (auction == null || auction.getId() == null) return;
        //add save bidders
        String sql = "INSERT INTO Auctions (id, item_id, seller_id, winner_id, start_time, end_time, starting_price, final_price, status, minimum_increment, buy_now_price) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE winner_id = VALUES(winner_id), end_time = VALUES(end_time), final_price = VALUES(final_price), status = VALUES(status)";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, auction.getId());
            stmt.setString(2, auction.getItem().getId());
            stmt.setString(3, auction.getSeller().getId());
            stmt.setString(4, auction.getCurrentWinnerId());
            stmt.setTimestamp(5, new Timestamp(auction.getStartTime()));
            stmt.setTimestamp(6, new Timestamp(auction.getEndTime()));
            stmt.setDouble(7, auction.getStartPrice());
            stmt.setDouble(8, auction.getCurrentPrice());
            stmt.setString(9, auction.getStatus() != null ? auction.getStatus().name() : "OPEN");
            stmt.setDouble(10, auction.getMinimumIncrement());
            stmt.setDouble(11, auction.getBuyNowPrice());

            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("DB error when saving auction auctionId={}", auction.getId(), e);
        }
    }

    @Override
    public Auction findById(String auctionId) {
        String sql = "SELECT * FROM Auctions WHERE id = ?";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, auctionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToAuction(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("DB error when finding auction by id auctionId={}", auctionId, e);
        }
        return null;
    }

    @Override
    public boolean delete(String auctionId) {
        String sql = "DELETE FROM Auctions WHERE id = ?";
        try (Connection connection = databaseConnection.getConnection();
            PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, auctionId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("DB error when deleting auction auctionId={}", auctionId, e);
        }
        return false;
    }

    private Auction mapRowToAuction(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String itemId = rs.getString("item_id");
        String sellerId = rs.getString("seller_id");
        String winnerId = rs.getString("winner_id");
        long startTime = rs.getTimestamp("start_time").getTime();
        long endTime = rs.getTimestamp("end_time").getTime();
        double startingPrice = rs.getDouble("starting_price");
        double finalPrice = rs.getDouble("final_price");
        String statusStr = rs.getString("status");
        long createdAt = rs.getTimestamp("created_at").getTime();

        Item item = itemRepository.findById(itemId);
        Member seller = (Member) userRepository.findById(sellerId);

        if (item == null || seller == null) return null;

        Auction auction = new Auction(id, item, seller, startingPrice, startTime, endTime, createdAt);
        auction.setCurrentWinnerId(winnerId);
        auction.setCurrentPrice(finalPrice > 0 ? finalPrice : startingPrice);
        if (statusStr != null) {
            auction.setStatus(AuctionStatus.valueOf(statusStr));
        }
        return auction;
    }

    @Override
    public List<Auction> findAll() {
        List<Auction> auctions = new ArrayList<>();
        String sql = "SELECT * FROM Auctions";

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Auction auction = mapRowToAuction(rs);
                if (auction != null) {
                    auctions.add(auction);
                }
            }
        } catch (SQLException e) {
            logger.error("DB error when finding all auctions", e);
        }
        return auctions;
    }
}
