package vn.io.huangnosimp.repository;

import vn.io.huangnosimp.connection.DatabaseConnection;
import vn.io.huangnosimp.model.Auction;
import vn.io.huangnosimp.model.AutoBidConfig;
import vn.io.huangnosimp.model.Member;
import vn.io.huangnosimp.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AutoBidRepository implements IAutoBidRepository {
    private static final Logger logger = LoggerFactory.getLogger(AutoBidRepository.class);

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
            logger.info(
                    "Saved autobid config bidderId={} auctionId={} maxBid={} increment={}",
                    memberId, auctionId, config.getMaxBid(), config.getIncrement()
            );
        } catch (SQLException e) {
            logger.error(
                    "DB error when saving autobid config bidderId={} auctionId={}",
                    memberId, auctionId, e
            );
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
            int deletedRows = ps.executeUpdate();
            logger.info(
                    "Deleted autobid config bidderId={} auctionId={} deletedRows={}",
                    memberId, auctionId, deletedRows
            );

        } catch (SQLException e) {
            logger.error(
                    "DB error when deleting autobid config bidderId={} auctionId={}",
                    memberId, auctionId, e
            );
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
                } else {
                    logger.warn(
                            "Skipped autobid config during warmup bidderId={} auctionId={} userFound={} auctionFound={}",
                            bidderId, auctionId, user != null, auction != null
                    );
                }
            }
            logger.info("Loaded autobid configs from database count={}", count);

        } catch (SQLException e) {
            logger.error("DB error when loading autobid configs from database", e);
        }
    }
}
