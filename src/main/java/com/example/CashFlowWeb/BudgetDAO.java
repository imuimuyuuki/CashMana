package com.example.CashFlowWeb;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BudgetDAO {

    /**
     * 指定された月の支出カテゴリに対する予算と実績を取得します。
     */
    public List<Budget> getBudgetsForMonth(String yearMonth) {
        List<Budget> budgetStatusList = new ArrayList<>();
        String sql = "SELECT " +
                     "  c.id AS category_id, c.name AS category_name, " +
                     "  COALESCE(b.amount, 0) AS budget_amount, " +
                     "  COALESCE(t.total_spent, 0) AS actual_amount " +
                     "FROM categories c " +
                     "LEFT JOIN (SELECT category_id, amount FROM budgets WHERE year_month = ?) b ON c.id = b.category_id " +
                     "LEFT JOIN (SELECT category_id, SUM(amount) AS total_spent FROM transactions WHERE strftime('%Y-%m', date) = ? AND type = 'EXPENSE' GROUP BY category_id) t ON c.id = t.category_id " +
                     "WHERE c.type = 'EXPENSE' ORDER BY c.name";

        try (Connection conn = DBManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, yearMonth);
            pstmt.setString(2, yearMonth);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Budget budget = new Budget();
                    budget.setCategoryId(rs.getInt("category_id"));
                    budget.setCategoryName(rs.getString("category_name"));
                    budget.setBudgetAmount(rs.getDouble("budget_amount"));
                    budget.setActualAmount(rs.getDouble("actual_amount"));
                    budgetStatusList.add(budget);
                }
            }
        } catch (SQLException e) {
            System.err.println("予算データ取得エラー: " + e.getMessage());
        }
        return budgetStatusList;
    }

    /**
     * 予算を保存または更新します。
     * SQLiteの "INSERT OR REPLACE" を利用して、存在すれば更新、なければ挿入します。
     */
    public boolean saveOrUpdateBudget(String yearMonth, int categoryId, double amount) {
        String sql = "INSERT OR REPLACE INTO budgets (id, year_month, category_id, amount) " +
                     "VALUES ((SELECT id FROM budgets WHERE year_month = ? AND category_id = ?), ?, ?, ?)";
        try (Connection conn = DBManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, yearMonth);
            pstmt.setInt(2, categoryId);
            pstmt.setString(3, yearMonth);
            pstmt.setInt(4, categoryId);
            pstmt.setDouble(5, amount);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("予算保存エラー: " + e.getMessage());
            return false;
        }
    }
}