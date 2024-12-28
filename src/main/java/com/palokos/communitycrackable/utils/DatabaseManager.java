package com.palokos.communitycrackable.utils;

import com.eu.habbo.Emulator;
import com.palokos.communitycrackable.CommunityCrackable;
import com.palokos.communitycrackable.models.RewardTier;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseManager {
    public void createTables() {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection()) {
            // Create crackables table
            connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS community_crackables (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "furniture_id INT NOT NULL, " +
                "sprite_id INT NOT NULL, " +
                "total_hits INT NOT NULL, " +
                "current_hits INT NOT NULL DEFAULT 0, " +
                "time_limit INT NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")").execute();
                
            // Create rewards table
            connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS community_crackables_rewards (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "crackable_id INT NOT NULL, " +
                "min_hits INT NOT NULL, " +
                "reward_type ENUM('credits', 'duckets', 'diamonds', 'badge', 'furniture') NOT NULL, " +
                "reward_id INT, " +
                "reward_amount INT NOT NULL, " +
                "FOREIGN KEY (crackable_id) REFERENCES community_crackables(id) ON DELETE CASCADE" +
                ")").execute();
                
            // Create participants table
            connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS community_crackables_participants (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "crackable_id INT NOT NULL, " +
                "user_id INT NOT NULL, " +
                "hits INT NOT NULL DEFAULT 0, " +
                "FOREIGN KEY (crackable_id) REFERENCES community_crackables(id) ON DELETE CASCADE" +
                ")").execute();

            // Create offline rewards table
            connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS community_crackables_offline_rewards (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id INT NOT NULL, " +
                "reward_type ENUM('credits', 'duckets', 'diamonds', 'badge', 'furniture') NOT NULL, " +
                "reward_id INT, " +
                "reward_amount INT NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")").execute();
        } catch (SQLException e) {
            Emulator.getLogging().logErrorLine(e);
        }
    }
    
    public CommunityCrackable createCommunityCrackable(int furnitureId, int spriteId, int totalHits, int timeLimit) throws SQLException {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO community_crackables (furniture_id, sprite_id, total_hits, time_limit) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            
            statement.setInt(1, furnitureId);
            statement.setInt(2, spriteId);
            statement.setInt(3, totalHits);
            statement.setInt(4, timeLimit);
            statement.execute();
            
            ResultSet keys = statement.getGeneratedKeys();
            if (keys.next()) {
                return loadCrackable(keys.getInt(1));
            }
        }
        throw new SQLException("Failed to create community crackable");
    }

    public void addCrackableReward(int crackableId, int minHits, String rewardType, int rewardId, int amount) {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO community_crackables_rewards (crackable_id, min_hits, reward_type, reward_id, reward_amount) VALUES (?, ?, ?, ?, ?)")) {
            
            statement.setInt(1, crackableId);
            statement.setInt(2, minHits);
            statement.setString(3, rewardType);
            statement.setInt(4, rewardId);
            statement.setInt(5, amount);
            statement.execute();
        } catch (SQLException e) {
            Emulator.getLogging().logErrorLine(e);
        }
    }

    public void updateParticipant(int crackableId, int userId, int hits) {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO community_crackables_participants (crackable_id, user_id, hits) " +
                "VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE hits = ?")) {
            
            statement.setInt(1, crackableId);
            statement.setInt(2, userId);
            statement.setInt(3, hits);
            statement.setInt(4, hits);
            statement.execute();
        } catch (SQLException e) {
            Emulator.getLogging().logErrorLine(e);
        }
    }

    public void storeOfflineReward(int userId, RewardTier reward) {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO community_crackables_offline_rewards (user_id, reward_type, reward_id, reward_amount) " +
                "VALUES (?, ?, ?, ?)")) {
            
            statement.setInt(1, userId);
            statement.setString(2, reward.getRewardType());
            statement.setInt(3, reward.getRewardId());
            statement.setInt(4, reward.getRewardAmount());
            statement.execute();
        } catch (SQLException e) {
            Emulator.getLogging().logErrorLine(e);
        }
    }

    public CommunityCrackable loadCrackable(int id) {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(
                "SELECT c.*, r.min_hits, r.reward_type, r.reward_id, r.reward_amount " +
                "FROM community_crackables c " +
                "LEFT JOIN community_crackables_rewards r ON c.id = r.crackable_id " +
                "WHERE c.id = ?")) {
            
            statement.setInt(1, id);
            ResultSet result = statement.executeQuery();
            
            if (result.next()) {
                int furnitureId = result.getInt("furniture_id");
                int spriteId = result.getInt("sprite_id");
                int totalHits = result.getInt("total_hits");
                int timeLimit = result.getInt("time_limit");
                List<RewardTier> rewards = new ArrayList<>();

                do {
                    if (result.getInt("min_hits") > 0) {
                        rewards.add(new RewardTier(
                            result.getInt("min_hits"),
                            result.getString("reward_type"),
                            result.getInt("reward_id"),
                            result.getInt("reward_amount")
                        ));
                    }
                } while (result.next());

                return new CommunityCrackable(id, furnitureId, spriteId, totalHits, timeLimit, rewards);
            }
        } catch (SQLException e) {
            Emulator.getLogging().logErrorLine(e);
        }
        return null;
    }

    public Map<Integer, CommunityCrackable> loadAllActiveCrackables() {
        Map<Integer, CommunityCrackable> crackables = new ConcurrentHashMap<>();
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(
                "SELECT DISTINCT id FROM community_crackables " +
                "WHERE created_at >= DATE_SUB(NOW(), INTERVAL time_limit MINUTE)")) {
            
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                CommunityCrackable crackable = loadCrackable(result.getInt("id"));
                if (crackable != null) {
                    crackables.put(crackable.getId(), crackable);
                }
            }
        } catch (SQLException e) {
            Emulator.getLogging().logErrorLine(e);
        }
        return crackables;
    }

    public void deleteExpiredCrackables() {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection()) {
            // Due to ON DELETE CASCADE, we only need to delete from the main table
            connection.prepareStatement(
                "DELETE FROM community_crackables " +
                "WHERE created_at < DATE_SUB(NOW(), INTERVAL time_limit MINUTE)"
            ).execute();
        } catch (SQLException e) {
            Emulator.getLogging().logErrorLine(e);
        }
    }
}
