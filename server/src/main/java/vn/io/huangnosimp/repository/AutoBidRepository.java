package vn.io.huangnosimp.repository;

import vn.io.huangnosimp.connection.DatabaseConnection;
import vn.io.huangnosimp.model.Auction;
import vn.io.huangnosimp.model.AutoBidConfig;
import vn.io.huangnosimp.model.Member;
import vn.io.huangnosimp.model.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AutoBidRepository implements IAutoBidRepository {

    private final Map<String, Map<String, AutoBidConfig>> storage = new ConcurrentHashMap<>();

    private final IUserRepository userRepository;
    private final IAuctionRepository auctionRepository;
    private final DatabaseConnection databaseConnection;

    public AutoBidRepository(IUserRepository userRepository,
                             IAuctionRepository auctionRepository,
                             DatabaseConnection databaseConnection) {
        this.userRepository = userRepository;
        this.auctionRepository = auctionRepository;
        this.databaseConnection = databaseConnection;

        loadAllFromDatabase();
    }

    @Override
    public void save(AutoBidConfig config) {
        String auctionId = config.getAuction().getId();
        String memberId = config.getBidder().getId();

        storage.computeIfAbsent(auctionId, k -> new ConcurrentHashMap<>())
                .put(memberId, config);

        String sql = "INSERT INTO auto_bids (bidder_id, auction_id, max_bid, increment, registered_at) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE max_bid = ?, increment = ?, registered_at = ?";

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, memberId);
            ps.setString(2, auctionId);
            ps.setDouble(3, config.getMaxBid());
            ps.setDouble(4, config.getIncrement());
            ps.setTimestamp(5, Timestamp.valueOf(config.getRegisteredAt()));
            ps.setDouble(6, config.getMaxBid());
            ps.setDouble(7, config.getIncrement());
            ps.setTimestamp(8, Timestamp.valueOf(config.getRegisteredAt()));

            ps.executeUpdate();
            System.out.println("[Repo] Synced AutoBid to MySQL for user: " + config.getBidder().getUsername());
        } catch (SQLException e) {
            System.err.println("[Repo Error] Failed to sync AutoBid to MySQL: " + e.getMessage());
        }
    }

    @Override
    public List<AutoBidConfig> findByAuctionId(String auctionId) {
        Map<String, AutoBidConfig> auctionConfigs = storage.get(auctionId);

        if (auctionConfigs == null || auctionConfigs.isEmpty()) {
            return new ArrayList<>();
        }

        return new ArrayList<>(auctionConfigs.values());
    }

    @Override
    public AutoBidConfig findByMemberAndAuction(String memberId, String auctionId) {
        Map<String, AutoBidConfig> auctionConfigs = storage.get(auctionId);

        if (auctionConfigs == null) {
            return null;
        }

        return auctionConfigs.get(memberId);
    }

    @Override
    public void delete(String memberId, String auctionId) {
        Map<String, AutoBidConfig> auctionConfigs = storage.get(auctionId);
        if (auctionConfigs != null) {
            auctionConfigs.remove(memberId);
        }

        String sql = "DELETE FROM auto_bids WHERE bidder_id = ? AND auction_id = ?";

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, memberId);
            ps.setString(2, auctionId);
            ps.executeUpdate();
            System.out.println("[Repo] Deleted AutoBid from MySQL for user ID: " + memberId);

        } catch (SQLException e) {
            System.err.println("[Repo Error] Failed to delete AutoBid from MySQL: " + e.getMessage());
        }
    }

    private void loadAllFromDatabase() {
        String sql = "SELECT * FROM auto_bids";
        int count = 0;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String bidderId = rs.getString("bidder_id");
                String auctionId = rs.getString("auction_id");
                double maxBid = rs.getDouble("max_bid");
                double increment = rs.getDouble("increment");
                LocalDateTime registeredAt = rs.getTimestamp("registered_at").toLocalDateTime();

                User user = userRepository.findById(bidderId);
                Auction auction = auctionRepository.findById(auctionId);

                if (user instanceof Member bidder && auction != null) {
                    AutoBidConfig config = new AutoBidConfig(bidder, auction, maxBid, increment, registeredAt);

                    storage.computeIfAbsent(auctionId, k -> new ConcurrentHashMap<>())
                            .put(bidderId, config);
                    count++;
                }
            }
            System.out.println("[Repo] Successfully warmed up " + count + " AutoBid configs from DB to RAM!");

        } catch (SQLException e) {
            System.err.println("[Repo Error] Critical! Failed to load AutoBids from Database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}