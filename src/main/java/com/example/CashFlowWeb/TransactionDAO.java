package com.example.CashFlowWeb;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {
    
    // ★引数に goalId を追加
    public boolean addTransaction(int userId, LocalDate date, double amount, String type, int categoryId, Integer goalId, boolean isFuture, boolean isExtraordinary) {
        // ★goal_id を追加
        String sql = "INSERT INTO transactions(user_id, date, amount, type, category_id, goal_id, is_future, is_extraordinary) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, date.toString());
            pstmt.setDouble(3, amount);
            pstmt.setString(4, type.toUpperCase());
            pstmt.setInt(5, categoryId);
            
            // goalIdはnullの可能性があるためセット方法を分岐
            if (goalId != null && goalId > 0) {
                pstmt.setInt(6, goalId);
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }

            pstmt.setBoolean(7, isFuture);
            pstmt.setBoolean(8, isExtraordinary); 
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("データ登録エラー: " + e.getMessage());
            return false;
        }
    }
    
    public List<Transaction> getAllTransactions(int userId) {
        return getFilteredTransactions(userId, null, null, null, null);
    }

    public List<Transaction> getFilteredTransactions(int userId, LocalDate startDate, LocalDate endDate, Integer categoryId, String type) {
        List<Transaction> transactions = new ArrayList<>();
        
        // ★goalsテーブルをJOINして目標名も取得
        StringBuilder sql = new StringBuilder(
            "SELECT t.id, t.date, t.amount, t.type, t.category_id, c.name AS category_name, " +
            "t.goal_id, g.name AS goal_name, " + // ★追加
            "t.is_future, t.is_extraordinary " +
            "FROM transactions t " +
            "LEFT JOIN categories c ON t.category_id = c.id " +
            "LEFT JOIN goals g ON t.goal_id = g.id " + // ★追加
            "WHERE t.user_id = ?");

        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (startDate != null) {
            sql.append(" AND t.date >= ?");
            params.add(startDate.toString());
        }
        if (endDate != null) {
            sql.append(" AND t.date <= ?");
            params.add(endDate.toString());
        }
        if (categoryId != null) {
            sql.append(" AND t.category_id = ?");
            params.add(categoryId);
        }
        if (type != null && !type.isEmpty()) {
            sql.append(" AND t.type = ?");
            params.add(type);
        }
        
        sql.append(" ORDER BY t.date DESC");

        try (Connection conn = DBManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Integer goalId は null の場合があるため getObject で取得
                    Integer gId = (Integer) rs.getObject("goal_id");

                    transactions.add(new Transaction(
                        rs.getInt("id"),
                        LocalDate.parse(rs.getString("date")),
                        rs.getDouble("amount"),
                        rs.getString("type"),
                        rs.getInt("category_id"),
                        rs.getString("category_name"),
                        gId, // ★
                        rs.getString("goal_name"), // ★
                        rs.getBoolean("is_future"),
                        rs.getBoolean("is_extraordinary")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("データ取得エラー: " + e.getMessage());
        }
        return transactions;
    }

    public Transaction getTransactionById(int id, int userId) {
        String sql = "SELECT t.id, t.date, t.amount, t.type, t.category_id, c.name AS category_name, t.goal_id, g.name AS goal_name, t.is_future, t.is_extraordinary " +
                     "FROM transactions t " +
                     "JOIN categories c ON t.category_id = c.id " +
                     "LEFT JOIN goals g ON t.goal_id = g.id " +
                     "WHERE t.id = ? AND t.user_id = ?";

        try (Connection conn = DBManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            pstmt.setInt(2, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Integer gId = (Integer) rs.getObject("goal_id");
                    return new Transaction(
                        rs.getInt("id"),
                        LocalDate.parse(rs.getString("date")),
                        rs.getDouble("amount"),
                        rs.getString("type"),
                        rs.getInt("category_id"),
                        rs.getString("category_name"),
                        gId,
                        rs.getString("goal_name"),
                        rs.getBoolean("is_future"),
                        rs.getBoolean("is_extraordinary")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
