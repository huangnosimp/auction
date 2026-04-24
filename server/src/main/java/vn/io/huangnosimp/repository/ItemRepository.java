package vn.io.huangnosimp.repository;

import vn.io.huangnosimp.database.DatabaseConnection;
import vn.io.huangnosimp.model.Art;
import vn.io.huangnosimp.model.Electronics;
import vn.io.huangnosimp.model.Item;
import vn.io.huangnosimp.model.Vehicle;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemRepository implements IItemRepository {
    private final DatabaseConnection databaseConnection;

    public ItemRepository(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }
    @Override
    public void save(Item item) {
        if (item == null || item.getId() == null)
            return;
        String sql = "INSERT INTO Items (id, created_at, owner_id, name, description, item_type, " +
                "artist, creation_year, brand, warranty_period, engine_type, mileage, custom_category, dynamic_attributes) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, item.getId());
            stmt.setTimestamp(2, new Timestamp(item.getCreatedAt()));
            stmt.setString(3, item.getOwnerId());
            stmt.setString(4, item.getName());
            stmt.setString(5, item.getDescription());

            String itemType = item.getClass().getSimpleName().toUpperCase();
            stmt.setString(6, itemType);

            stmt.setNull(7, java.sql.Types.VARCHAR);
            stmt.setNull(8, java.sql.Types.INTEGER);
            stmt.setNull(9, java.sql.Types.VARCHAR);
            stmt.setNull(10, java.sql.Types.INTEGER);
            stmt.setNull(11, java.sql.Types.VARCHAR);
            stmt.setNull(12, java.sql.Types.INTEGER);
            stmt.setNull(13, java.sql.Types.VARCHAR);
            stmt.setNull(14, java.sql.Types.VARCHAR);

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
        } catch (SQLException e) {
            System.err.println("DB error when saving item: " + e.getMessage());
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
                    return mapRowToItem(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("DB error in findById: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void updateOwner(String itemId, String ownerId) {
        String sql = "UPDATE Items SET owner_id = ? WHERE id = ?";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, ownerId);
            stmt.setString(2, itemId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("DB error when updating item owner: " + e.getMessage());
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
            System.err.println("DB error when deleting item: " + e.getMessage());
        }
        return false;
    }

    private Item mapRowToItem(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        long createdAt = rs.getTimestamp("created_at").getTime();
        String ownerId = rs.getString("owner_id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        String itemType = rs.getString("item_type");

        Item item = null;
        if ("ART".equals(itemType)) {
            String artist = rs.getString("artist");
            int creationYear = rs.getInt("creation_year");
            item = new Art(ownerId, name, description, artist, creationYear);
        } else if ("ELECTRONICS".equals(itemType)) {
            String brand = rs.getString("brand");
            int warrantyPeriod = rs.getInt("warranty_period");
            item = new Electronics(ownerId, name, description, brand, warrantyPeriod);
        } else if ("VEHICLE".equals(itemType)) {
            String engineType = rs.getString("engine_type");
            int mileage = rs.getInt("mileage");
            item = new Vehicle(ownerId, name, description, engineType, mileage);
        }


        if (item != null) {
            item.setId(id);
            item.setCreatedAt(createdAt);
        }
        return item;
    }
}
