package vn.io.huangnosimp.repository;

import vn.io.huangnosimp.connection.DatabaseConnection;
import vn.io.huangnosimp.dto.response.AuctionCardDTO;
import vn.io.huangnosimp.dto.response.DashboardResponseDTO;
import vn.io.huangnosimp.dto.response.AuctionDetailResponseDTO;
import vn.io.huangnosimp.dto.response.BidHistoryDTO;
import vn.io.huangnosimp.dto.response.PricePointDTO;
import vn.io.huangnosimp.enums.ItemCondition;
import vn.io.huangnosimp.enums.ItemType;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatisticRepository implements IStatisticRepository {
    private static final Logger logger = LoggerFactory.getLogger(StatisticRepository.class);
    private final DatabaseConnection databaseConnection;

    public StatisticRepository(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    @Override
    public DashboardResponseDTO getUserScalarStatistics(String userId) {
        String sql = "SELECT " +
                "u.username, u.email, u.account_balance, " +
                "(SELECT COUNT(*) FROM auctions a WHERE a.winner_id = u.id AND a.status = 'RUNNING') AS winning_bids, " +
                "(SELECT COUNT(DISTINCT a.id) FROM auctions a JOIN bidtransactions bt ON a.id = bt.auction_id WHERE bt.bidder_id = u.id AND a.winner_id != u.id AND a.status = 'RUNNING') AS out_bids, " +
                "(SELECT COUNT(*) FROM auctions a WHERE a.winner_id = u.id AND a.status = 'PAID') AS won_total " +
                "FROM users u " +
                "WHERE u.id = ?";
                
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                String username = rs.getString("username");
                String email = rs.getString("email");
                double balance = rs.getDouble("account_balance");
                int winningBids = rs.getInt("winning_bids");
                int outBids = rs.getInt("out_bids");
                int wonTotal = rs.getInt("won_total");
                return new DashboardResponseDTO(balance, 0, winningBids, outBids, wonTotal, Collections.emptyList(), username, email);
            }
        } catch (SQLException e) {
            logger.error("DB error when fetching user scalar statistics userId={}", userId, e);
        }
        return new DashboardResponseDTO(0.0, 0, 0, 0, 0, Collections.emptyList(), null, null);
    }

    @Override
    public List<AuctionCardDTO> getMyAuctionCard(String userId) {
        List<AuctionCardDTO> rooms = new java.util.ArrayList<>();
        String sql = "SELECT a.id AS auction_id, i.id AS item_id, i.name AS product_name, " +
                     "IFNULL(NULLIF(a.final_price, 0), a.starting_price) AS current_price, " +
                     "(SELECT IFNULL(MAX(bid_amount), 0) FROM bidtransactions bt WHERE bt.auction_id = a.id AND bt.bidder_id = ?) AS your_bid, " +
                     "a.start_time AS start_time, a.end_time AS end_time, " +
                     "(SELECT COUNT(*) FROM bidtransactions bt2 WHERE bt2.auction_id = a.id) AS bid_count, " +
                     "(SELECT COUNT(DISTINCT bt3.bidder_id) FROM bidtransactions bt3 WHERE bt3.auction_id = a.id) AS bidder_count " +
                     "FROM auctions a " +
                     "JOIN auctionparticipants ap ON a.id = ap.auction_id " +
                     "JOIN items i ON a.item_id = i.id " +
                     "WHERE ap.user_id = ? AND a.status IN ('OPEN', 'RUNNING')";
                     
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            statement.setString(2, userId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                rooms.add(mapRowToAuctionCard(connection, rs));
            }
        } catch (SQLException e) {
            logger.error("DB error when fetching active rooms userId={}", userId, e);
        }
        return rooms;
    }

    @Override
    public AuctionDetailResponseDTO getAuctionDetail(String auctionId) {
        AuctionDetailResponseDTO result = null;

        String auctionSql = "SELECT a.id, a.starting_price, a.final_price, a.start_time, a.end_time, a.status, a.minimum_increment, a.buy_now_price, " +
                "i.name AS product_name, i.item_type, i.conditions, i.description, " +
                "w.username AS winner_username, " +
                "(SELECT COUNT(*) FROM auctionparticipants WHERE auction_id = a.id) AS participant_count, " +
                "(SELECT COUNT(*) FROM bidtransactions WHERE auction_id = a.id) AS bid_count " +
                "FROM auctions a " +
                "JOIN items i ON a.item_id = i.id " +
                "LEFT JOIN users w ON a.winner_id = w.id " +
                "WHERE a.id = ?";

        String bidsSql = "SELECT b.bid_amount, b.bid_time, u.username " +
                "FROM bidtransactions b " +
                "JOIN users u ON b.bidder_id = u.id " +
                "WHERE b.auction_id = ? ORDER BY b.bid_time ASC";

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement auctionStmt = connection.prepareStatement(auctionSql);
             PreparedStatement bidsStmt = connection.prepareStatement(bidsSql)) {

            auctionStmt.setString(1, auctionId);
            ResultSet rsAuction = auctionStmt.executeQuery();

            if (rsAuction.next()) {
                String productName = rsAuction.getString("product_name");
                String itemTypeStr = rsAuction.getString("item_type");
                String conditionStr = rsAuction.getString("conditions");
                String description = rsAuction.getString("description");
                double startPrice = rsAuction.getDouble("starting_price");
                double finalPrice = rsAuction.getDouble("final_price");
                long startTime = rsAuction.getTimestamp("start_time").getTime();
                long endTime = rsAuction.getTimestamp("end_time").getTime();
                String leadBidder = rsAuction.getString("winner_username");
                int participantCount = rsAuction.getInt("participant_count");
                int bidCount = rsAuction.getInt("bid_count");

                ItemType category = null;
                try { category = ItemType.valueOf(itemTypeStr); } catch (Exception ignored) {}

                ItemCondition condition = null;
                try { condition = ItemCondition.valueOf(conditionStr); } catch (Exception ignored) {}

                double currentPrice = finalPrice > 0 ? finalPrice : startPrice;
                double bidIncrement = rsAuction.getDouble("minimum_increment");
                double buyNowPrice = rsAuction.getDouble("buy_now_price");
                double minNextBid = currentPrice + bidIncrement;

                List<BidHistoryDTO> bidHistory = new ArrayList<>();
                List<PricePointDTO> priceHistory = new ArrayList<>();

                priceHistory.add(new PricePointDTO(startTime, startPrice));

                bidsStmt.setString(1, auctionId);
                ResultSet rsBids = bidsStmt.executeQuery();
                long lastBidTime = startTime;

                while (rsBids.next()) {
                    double bidAmount = rsBids.getDouble("bid_amount");
                    long bidTime = rsBids.getTimestamp("bid_time").getTime();
                    String bidderName = rsBids.getString("username");

                    bidHistory.add(0, new BidHistoryDTO(bidderName, bidAmount, bidTime));
                    priceHistory.add(new PricePointDTO(bidTime, bidAmount));
                    lastBidTime = bidTime;
                }

                result = new AuctionDetailResponseDTO(
                        productName, category, condition, description,
                        startPrice, bidIncrement, buyNowPrice, startPrice,
                        startTime, endTime, currentPrice, minNextBid,
                        leadBidder, lastBidTime, participantCount, bidCount,
                        bidHistory, priceHistory
                );
            }

        } catch (SQLException e) {
            logger.error("DB error when fetching auction details auctionId={}", auctionId, e);
        }

        return result;
    }

    @Override
    public AuctionCardDTO getJoiningAuctionCard(String auctionId) {
        String sql = "SELECT a.id AS auction_id, i.id AS item_id, i.name AS product_name, " +
                     "IFNULL(NULLIF(a.final_price, 0), a.starting_price) AS current_price, " +
                     "0 AS your_bid, " +
                     "a.start_time AS start_time, a.end_time AS end_time, " +
                     "(SELECT COUNT(*) FROM bidtransactions bt WHERE bt.auction_id = a.id) AS bid_count, " +
                     "(SELECT COUNT(DISTINCT bt2.bidder_id) FROM bidtransactions bt2 WHERE bt2.auction_id = a.id) AS bidder_count " +
                     "FROM auctions a " +
                     "JOIN items i ON a.item_id = i.id " +
                     "WHERE a.id = ?";
                     
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, auctionId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return mapRowToAuctionCard(connection, rs);
            }
        } catch (SQLException e) {
            logger.error("DB error when fetching auction card auctionId={}", auctionId, e);
        }
        return null;
    }

    @Override
    public List<AuctionCardDTO> getPublicAuctionCard(int quantity) {
        List<AuctionCardDTO> rooms = new ArrayList<>();
        String sql = "SELECT a.id AS auction_id, i.id AS item_id, i.name AS product_name, " +
                     "IFNULL(NULLIF(a.final_price, 0), a.starting_price) AS current_price, " +
                     "0 AS your_bid, " +
                     "a.start_time AS start_time, a.end_time AS end_time, " +
                     "(SELECT COUNT(*) FROM bidtransactions bt WHERE bt.auction_id = a.id) AS bid_count, " +
                     "(SELECT COUNT(DISTINCT bt2.bidder_id) FROM bidtransactions bt2 WHERE bt2.auction_id = a.id) AS bidder_count " +
                     "FROM auctions a " +
                     "JOIN items i ON a.item_id = i.id " +
                     "WHERE a.status IN ('OPEN', 'RUNNING') " +
                     "ORDER BY a.start_time DESC " +
                     "LIMIT ?";
                     
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, quantity);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                rooms.add(mapRowToAuctionCard(connection, rs));
            }
        } catch (SQLException e) {
            logger.error("DB error when fetching public auction cards quantity={}", quantity, e);
        }
        return rooms;
    }

    @Override
    public List<AuctionCardDTO> getPostedAuctionCard(String userId, int amount) {
        List<AuctionCardDTO> rooms = new ArrayList<>();
        String sql = "SELECT a.id AS auction_id, i.id AS item_id, i.name AS product_name, " +
                     "IFNULL(NULLIF(a.final_price, 0), a.starting_price) AS current_price, " +
                     "0 AS your_bid, " +
                     "a.start_time AS start_time, a.end_time AS end_time, " +
                     "(SELECT COUNT(*) FROM bidtransactions bt WHERE bt.auction_id = a.id) AS bid_count, " +
                     "(SELECT COUNT(DISTINCT bt2.bidder_id) FROM bidtransactions bt2 WHERE bt2.auction_id = a.id) AS bidder_count " +
                     "FROM auctions a " +
                     "JOIN items i ON a.item_id = i.id " +
                     "WHERE a.seller_id = ? AND a.status IN ('OPEN', 'RUNNING') " +
                     "ORDER BY a.start_time DESC " +
                     "LIMIT ?";
                     
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            statement.setInt(2, amount);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                rooms.add(mapRowToAuctionCard(connection, rs));
            }
        } catch (SQLException e) {
            logger.error("DB error when fetching posted auction cards userId={} amount={}", userId, amount, e);
        }
        return rooms;
    }

    @Override
    public List<AuctionCardDTO> getWonAuction(String userId, int amount) {
        List<AuctionCardDTO> rooms = new ArrayList<>();
        String sql = "SELECT a.id AS auction_id, i.id AS item_id, i.name AS product_name, " +
                     "IFNULL(NULLIF(a.final_price, 0), a.starting_price) AS current_price, " +
                     "(SELECT IFNULL(MAX(bid_amount), 0) FROM bidtransactions bt WHERE bt.auction_id = a.id AND bt.bidder_id = ?) AS your_bid, " +
                     "a.start_time AS start_time, a.end_time AS end_time, " +
                     "(SELECT COUNT(*) FROM bidtransactions bt2 WHERE bt2.auction_id = a.id) AS bid_count, " +
                     "(SELECT COUNT(DISTINCT bt3.bidder_id) FROM bidtransactions bt3 WHERE bt3.auction_id = a.id) AS bidder_count " +
                     "FROM auctions a " +
                     "JOIN items i ON a.item_id = i.id " +
                     "WHERE a.winner_id = ? AND a.status IN ('FINISHED', 'PAID') " +
                     "ORDER BY a.end_time DESC " +
                     "LIMIT ?";

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            statement.setString(2, userId);
            statement.setInt(3, amount);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                rooms.add(mapRowToAuctionCard(connection, rs));
            }
        } catch (SQLException e) {
            logger.error("DB error when fetching won auctions userId={} amount={}", userId, amount, e);
        }
        return rooms;
    }

    @Override
    public List<AuctionCardDTO> getEndedPostedAuction(String userId, int amount) {
        List<AuctionCardDTO> rooms = new ArrayList<>();
        String sql = "SELECT a.id AS auction_id, i.id AS item_id, i.name AS product_name, " +
                     "IFNULL(NULLIF(a.final_price, 0), a.starting_price) AS current_price, " +
                     "0 AS your_bid, " +
                     "a.start_time AS start_time, a.end_time AS end_time, " +
                     "(SELECT COUNT(*) FROM bidtransactions bt WHERE bt.auction_id = a.id) AS bid_count, " +
                     "(SELECT COUNT(DISTINCT bt2.bidder_id) FROM bidtransactions bt2 WHERE bt2.auction_id = a.id) AS bidder_count " +
                     "FROM auctions a " +
                     "JOIN items i ON a.item_id = i.id " +
                     "WHERE a.seller_id = ? AND a.status IN ('FINISHED', 'PAID') " +
                     "ORDER BY a.end_time DESC " +
                     "LIMIT ?";

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            statement.setInt(2, amount);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                rooms.add(mapRowToAuctionCard(connection, rs));
            }
        } catch (SQLException e) {
            logger.error("DB error when fetching ended posted auctions userId={} amount={}", userId, amount, e);
        }
        return rooms;
    }

    private AuctionCardDTO mapRowToAuctionCard(Connection connection, ResultSet rs) throws SQLException {
        String auctionId = rs.getString("auction_id");
        String itemId = rs.getString("item_id");
        String productName = rs.getString("product_name");
        double currentPrice = rs.getDouble("current_price");
        double yourBid = rs.getDouble("your_bid");
        long startTime = rs.getTimestamp("start_time").getTime();
        long endTime = rs.getTimestamp("end_time").getTime();
        int bidCount = rs.getInt("bid_count");
        int bidderCount = rs.getInt("bidder_count");
        List<String> imageUrl = findImageUrlsByItemId(connection, itemId);

        return new AuctionCardDTO(
                auctionId,
                productName,
                currentPrice,
                yourBid,
                startTime,
                endTime,
                bidCount,
                bidderCount,
                imageUrl
        );
    }

    private List<String> findImageUrlsByItemId(Connection connection, String itemId) throws SQLException {
        List<String> imageUrls = new ArrayList<>();
        String sql = "SELECT image_url FROM ItemImages WHERE item_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, itemId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    imageUrls.add(rs.getString("image_url"));
                }
            }
        }

        return imageUrls;
    }
}
