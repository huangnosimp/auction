package vn.io.huangnosimp.repository;

import vn.io.huangnosimp.database.DatabaseConnection;

import java.sql.*;

public class StatisticRepository implements IStatisticRepository {
    private final DatabaseConnection databaseConnection;

    public StatisticRepository(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    @Override
    public double getAccountBalance(String userId) {
        String sql = "SELECT account_balance FROM users WHERE id = ?";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getDouble("account_balance");
            }
        } catch (SQLException e) {
            System.err.println("DB error when fetching account balance: " + e.getMessage());
        }
        return 0.0;
    }

    @Override
    public int getJoinedActiveRoomsCount(String userId) {
        String sql = "SELECT COUNT(*) FROM auctionparticipants ap JOIN auctions a ON ap.auction_id = a.id WHERE ap.user_id = ? AND a.status IN ('OPEN', 'RUNNING')";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("DB error when fetching joined active rooms count: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public int getWinningBidsCount(String userId) {
        String sql = "SELECT COUNT(*) FROM auctions WHERE winner_id = ? AND status = 'RUNNING'";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("DB error when fetching winning bids count: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public int getOutBidsCount(String userId) {
        String sql = "SELECT COUNT(DISTINCT a.id) FROM auctions a JOIN bidtransactions bt ON a.id = bt.auction_id WHERE bt.bidder_id = ? AND a.winner_id != ? AND a.status = 'RUNNING'";
        try (Connection connection = databaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            statement.setString(2, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("DB error when fetching outbids count: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public int getWonTotalCount(String userId) {
        String sql = "SELECT COUNT(*) FROM auctions WHERE winner_id = ? AND status = 'PAID'";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("DB error when fetching won total count: " + e.getMessage());
        }
        return 0;
    }
}
