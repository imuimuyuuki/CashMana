package com.example.CashFlowWeb;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GoalDAO {

    public List<Goal> getAllGoals(int userId) {
        List<Goal> goals = new ArrayList<>();
        
        // ★重要: goalsテーブルとtransactionsテーブルを結合し、
        // 紐付いている取引の合計額を current_amount (初期値) に加算して取得するSQL
        String sql = "SELECT g.id, g.name, g.target_amount, g.target_date, g.image_url, " +
                     "g.current_amount AS initial_amount, " + // 初期値
                     "COALESCE(SUM(t.amount), 0) AS accumulated_amount " + // 積立額の合計
                     "FROM goals g " +
                     "LEFT JOIN transactions t ON g.id = t.goal_id " +
                     "WHERE g.user_id = ? " +
                     "GROUP BY g.id " +
                     "ORDER BY g.target_date";
        
        try (Connection conn = DBManager.connect(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Goal goal = new Goal();
                    goal.setId(rs.getInt("id"));
                    goal.setName(rs.getString("name"));
                    goal.setTargetAmount(rs.getDouble("target_amount"));
                    
                    // ★初期値 + 積立額 = 現在の達成額
                    double totalCurrent = rs.getDouble("initial_amount") + rs.getDouble("accumulated_amount");
                    goal.setCurrentAmount(totalCurrent);
                    
                    goal.setTargetDate(rs.getString("target_date"));
                    goal.setImageUrl(rs.getString("image_url"));
                    goals.add(goal);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return goals;
    }

    public boolean addGoal(Goal goal, int userId) {
        String sql = "INSERT INTO goals(user_id, name, target_amount, current_amount, target_date, image_url) VALUES(?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBManager.connect(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, goal.getName());
            pstmt.setDouble(3, goal.getTargetAmount());
            pstmt.setDouble(4, goal.getCurrentAmount()); // ここは初期貯蓄額として保存される
            pstmt.setString(5, goal.getTargetDate());
            pstmt.setString(6, goal.getImageUrl());
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // updateGoal, deleteGoal は前回のままでOK（IDとUserIDで処理）
    // ...
    // 省略していますが、updateGoal, deleteGoalメソッドも前回のコードを含めてください
    // ...
    public boolean updateGoal(Goal goal, int userId) {
        String sql = "UPDATE goals SET name = ?, target_amount = ?, current_amount = ?, target_date = ?, image_url = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = DBManager.connect(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
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
        try (Connection conn = DBManager.connect(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
