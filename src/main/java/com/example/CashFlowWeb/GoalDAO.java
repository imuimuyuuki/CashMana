package com.example.CashFlowWeb;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GoalDAO {

    public List<Goal> getAllGoals() {
        List<Goal> goals = new ArrayList<>();
        String sql = "SELECT * FROM goals ORDER BY target_date";
        try (Connection conn = DBManager.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                goals.add(mapToGoal(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return goals;
    }

    public boolean addGoal(Goal goal) {
        String sql = "INSERT INTO goals(name, target_amount, current_amount, target_date, image_url) VALUES(?, ?, ?, ?, ?)";
        try (Connection conn = DBManager.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, goal.getName());
            pstmt.setDouble(2, goal.getTargetAmount());
            pstmt.setDouble(3, goal.getCurrentAmount());
            pstmt.setString(4, goal.getTargetDate());
            pstmt.setString(5, goal.getImageUrl());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateGoal(Goal goal) {
        String sql = "UPDATE goals SET name = ?, target_amount = ?, current_amount = ?, target_date = ?, image_url = ? WHERE id = ?";
        try (Connection conn = DBManager.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, goal.getName());
            pstmt.setDouble(2, goal.getTargetAmount());
            pstmt.setDouble(3, goal.getCurrentAmount());
            pstmt.setString(4, goal.getTargetDate());
            pstmt.setString(5, goal.getImageUrl());
            pstmt.setInt(6, goal.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean deleteGoal(int id) {
        String sql = "DELETE FROM goals WHERE id = ?";
        try (Connection conn = DBManager.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
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