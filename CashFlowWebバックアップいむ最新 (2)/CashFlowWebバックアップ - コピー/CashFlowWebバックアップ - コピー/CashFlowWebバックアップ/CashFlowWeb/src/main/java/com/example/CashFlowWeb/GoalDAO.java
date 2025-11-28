package com.example.CashFlowWeb;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GoalDAO {

    public List<Goal> getAllGoals(int userId) {
        List<Goal> goals = new ArrayList<>();
        String sql = "SELECT * FROM goals WHERE user_id = ? ORDER BY target_date";
        try (Connection conn = DBManager.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    goals.add(mapToGoal(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return goals;
    }

    public boolean addGoal(Goal goal, int userId) {
        String sql = "INSERT INTO goals(user_id, name, target_amount, current_amount, target_date, image_url) VALUES(?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBManager.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, goal.getName());
            pstmt.setDouble(3, goal.getTargetAmount());
            pstmt.setDouble(4, goal.getCurrentAmount());
            pstmt.setString(5, goal.getTargetDate());
            pstmt.setString(6, goal.getImageUrl());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateGoal(Goal goal, int userId) {
        String sql = "UPDATE goals SET name = ?, target_amount = ?, current_amount = ?, target_date = ?, image_url = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = DBManager.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, goal.getName());
            pstmt.setDouble(2, goal.getTargetAmount());
            pstmt.setDouble(3, goal.getCurrentAmount());
            pstmt.setString(4, goal.getTargetDate());
            pstmt.setString(5, goal.getImageUrl());
            pstmt.setInt(6, goal.getId());
            pstmt.setInt(7, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteGoal(int id, int userId) {
        String sql = "DELETE FROM goals WHERE id = ? AND user_id = ?";
        try (Connection conn = DBManager.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Goal mapToGoal(ResultSet rs) throws SQLException {
        Goal goal = new Goal();
        goal.setId(rs.getInt("id"));
        goal.setName(rs.getString("name"));
        goal.setTargetAmount(rs.getDouble("target_amount"));
        goal.setCurrentAmount(rs.getDouble("current_amount"));
        goal.setTargetDate(rs.getString("target_date"));
        goal.setImageUrl(rs.getString("image_url"));
        return goal;
    }
}