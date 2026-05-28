package vn.io.huangnosimp.repository;

import vn.io.huangnosimp.connection.DatabaseConnection;
import vn.io.huangnosimp.enums.ItemCondition;
import vn.io.huangnosimp.model.Art;
import vn.io.huangnosimp.model.Electronics;
import vn.io.huangnosimp.model.Item;
import vn.io.huangnosimp.model.Vehicle;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemRepository implements IItemRepository {
    private static final Logger logger = LoggerFactory.getLogger(ItemRepository.class);
    private final DatabaseConnection databaseConnection;

    public ItemRepository(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }
    @Override
    public void save(Item item) {
        if (item == null || item.getId() == null)
            return;
        String sql = "INSERT INTO Items (id, created_at, owner_id, name, description, item_type, " +
                "artist, creation_year, brand, warranty_period, engine_type, mileage, conditions) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseConnection.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                    stmt.setString(1, item.getId());
                    stmt.setTimestamp(2, new Timestamp(item.getCreatedAt()));
                    stmt.setString(3, item.getOwnerId());
                    stmt.setString(4, item.getName());
                    stmt.setString(5, item.getDescription());

                    String itemType = item.getClass().getSimpleName().toUpperCase();
                    stmt.setString(6, itemType);

                    stmt.setNull(7, Types.VARCHAR);
                    stmt.setNull(8, Types.INTEGER);
                    stmt.setNull(9, Types.VARCHAR);
                    stmt.setNull(10, Types.INTEGER);
                    stmt.setNull(11, Types.VARCHAR);
                    stmt.setNull(12, Types.INTEGER);

                    stmt.setString(13, item.getCondition().name());

                    switch (item) {
                        case Art art -> {
                            stmt.setString(7, art.getArtist());
                            stmt.setInt(8, art.getCreationYear());
                        }
                        case Electronics electronics -> {
                            stmt.setString(9, electronics.getBrand());
                            stmt.setInt(10, electronics.getWarrantyMonths());
                        }
                        case Vehicle vehicle -> {
                            stmt.setString(11, vehicle.getEngineType());
                            stmt.setInt(12, (int) vehicle.getMileage());
                        }
                        default -> {
                        }
                    }
                    stmt.executeUpdate();
                }
                saveImageUrls(conn, item);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException e) {
            logger.error("DB error when saving item itemId={}", item.getId(), e);
        }
    }
    @Override
    public Item findById(String itemId) {
        String sql = "SELECT * FROM Items WHERE id = ?";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, itemId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToItem(conn, rs);
                }
            }
        } catch (SQLException e) {
            logger.error("DB error when finding item by id itemId={}", itemId, e);
        }
        return null;
    }

    @Override
    public boolean updateOwner(String itemId, String ownerId) {
        String sql = "UPDATE Items SET owner_id = ? WHERE id = ?";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, ownerId);
            stmt.setString(2, itemId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("DB error when updating item owner itemId={} ownerId={}", itemId, ownerId, e);
            return false;
        }
    }

    @Override
    public boolean delete(String itemId) {
        String sql = "DELETE FROM Items WHERE id = ?";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, itemId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("DB error when deleting item itemId={}", itemId, e);
        }
        return false;
    }

    private void saveImageUrls(Connection conn, Item item) throws SQLException {
        List<String> imageUrls = item.getImageUrl();
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO ItemImages (item_id, image_url) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (String imageUrl : imageUrls) {
                if (imageUrl == null || imageUrl.isBlank()) {
                    continue;
                }
                stmt.setString(1, item.getId());
                stmt.setString(2, imageUrl);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private List<String> findImageUrlsByItemId(Connection conn, String itemId) throws SQLException {
        List<String> imageUrls = new ArrayList<>();
        String sql = "SELECT image_url FROM ItemImages WHERE item_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, itemId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    imageUrls.add(rs.getString("image_url"));
                }
            }
        }
        return imageUrls;
    }

    private Item mapRowToItem(Connection conn, ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        long createdAt = rs.getTimestamp("created_at").getTime();
        String ownerId = rs.getString("owner_id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        String itemType = rs.getString("item_type");
        ItemCondition conditions = ItemCondition.valueOf(rs.getString("conditions"));
        List<String> imageUrls = findImageUrlsByItemId(conn, id);

        Item item = null;
        if ("ART".equals(itemType)) {
            String artist = rs.getString("artist");
            int creationYear = rs.getInt("creation_year");
            item = new Art(ownerId, name, description, artist, creationYear, conditions, imageUrls);
        } else if ("ELECTRONICS".equals(itemType)) {
            String brand = rs.getString("brand");
            int warrantyPeriod = rs.getInt("warranty_period");
            item = new Electronics(ownerId, name, description, brand, warrantyPeriod, conditions, imageUrls);
        } else if ("VEHICLE".equals(itemType)) {
            String engineType = rs.getString("engine_type");
            int mileage = rs.getInt("mileage");
            item = new Vehicle(ownerId, name, description, engineType, mileage, conditions, imageUrls);
        }


        if (item != null) {
            item.setId(id);
            item.setCreatedAt(createdAt);
        }
        return item;
    }
}
