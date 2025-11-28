package com.example.CashFlowWeb;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TransactionDAO {

    public boolean addTransaction(int userId, LocalDate date, double amount, String type, int categoryId,
            boolean isFuture, boolean isExtraordinary) {
        String sql = "INSERT INTO transactions(user_id, date, amount, type, category_id, is_future, is_extraordinary) VALUES(?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBManager.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, date.toString());
            pstmt.setDouble(3, amount);
            pstmt.setString(4, type.toUpperCase());
            pstmt.setInt(5, categoryId);
            pstmt.setBoolean(6, isFuture);
            pstmt.setBoolean(7, isExtraordinary);

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

    public List<Transaction> getFilteredTransactions(int userId, LocalDate startDate, LocalDate endDate,
            Integer categoryId, String type) {
        List<Transaction> transactions = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT t.id, t.date, t.amount, t.type, t.category_id, c.name AS category_name, t.is_future, t.is_extraordinary "
                        +
                        "FROM transactions t " +
                        "LEFT JOIN categories c ON t.category_id = c.id " +
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
        if (type != null) {
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
                    transactions.add(new Transaction(
                            rs.getInt("id"),
                            LocalDate.parse(rs.getString("date")),
                            rs.getDouble("amount"),
                            rs.getString("type"),
                            rs.getInt("category_id"),
                            rs.getString("category_name"),
                            rs.getBoolean("is_future"),
                            rs.getBoolean("is_extraordinary")));
                }
            }
        } catch (SQLException e) {
            System.err.println("データ取得エラー: " + e.getMessage());
        }
        return transactions;
    }

    public Transaction getTransactionById(int id, int userId) {
        String sql = "SELECT t.id, t.date, t.amount, t.type, t.category_id, c.name AS category_name, t.is_future, t.is_extraordinary "
                +
                "FROM transactions t " +
                "JOIN categories c ON t.category_id = c.id " +
                "WHERE t.id = ? AND t.user_id = ?";

        try (Connection conn = DBManager.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.setInt(2, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Transaction(
                            rs.getInt("id"),
                            LocalDate.parse(rs.getString("date")),
                            rs.getDouble("amount"),
                            rs.getString("type"),
                            rs.getInt("category_id"),
                            rs.getString("category_name"),
                            rs.getBoolean("is_future"),
                            rs.getBoolean("is_extraordinary"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 【追加】削除機能
    public boolean deleteTransaction(int id, int userId) {
        String sql = "DELETE FROM transactions WHERE id = ? AND user_id = ?";
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

    // 【追加】現在の総資産残高を計算 (収入 - 支出)
    public double calculateCurrentBalance(int userId) {
        String sql = "SELECT " +
                "SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) - " +
                "SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) as balance " +
                "FROM transactions WHERE user_id = ? AND is_future = 0"; // 確定した取引のみ

        try (Connection conn = DBManager.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("balance");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    // 【追加】月別集計データの取得 (棒グラフ用)
    public List<MonthlySummary> getMonthlySummary(int userId, LocalDate startDate, LocalDate endDate) {
        List<MonthlySummary> summaries = new ArrayList<>();
        // SQLiteの strftime('%Y-%m', date) を使用して月ごとに集計
        String sql = "SELECT strftime('%Y-%m', date) as month, " +
                "SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) as total_income, " +
                "SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) as total_expense " +
                "FROM transactions " +
                "WHERE user_id = ? ";

        if (startDate != null)
            sql += " AND date >= ? ";
        if (endDate != null)
            sql += " AND date <= ? ";

        sql += "GROUP BY month ORDER BY month";

        try (Connection conn = DBManager.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int paramIndex = 1;
            pstmt.setInt(paramIndex++, userId);
            if (startDate != null)
                pstmt.setString(paramIndex++, startDate.toString());
            if (endDate != null)
                pstmt.setString(paramIndex++, endDate.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    summaries.add(new MonthlySummary(
                            rs.getString("month"),
                            rs.getDouble("total_income"),
                            rs.getDouble("total_expense")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return summaries;
    }
}