package com.example.CashFlowWeb;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BudgetDAO {

    /**
     * 指定された月の支出カテゴリに対する予算と実績を取得します。
     * ユーザーIDに基づいてフィルタリングを行います。
     */
    public List<Budget> getBudgetsForMonth(String yearMonth, int userId) {
        List<Budget> budgetStatusList = new ArrayList<>();

        // ★ categories テーブルを user_id でフィルタリングすることが最重要
        // ★ budgets テーブルも user_id で結合条件を追加
        String sql = "SELECT " +
                "  c.id AS category_id, c.name AS category_name, " +
                "  COALESCE(b.amount, 0) AS budget_amount, " +
                "  COALESCE(t.total_spent, 0) AS actual_amount " +
                "FROM categories c " +
                // 自分の予算データを結合
                "LEFT JOIN (SELECT category_id, amount FROM budgets WHERE year_month = ? AND user_id = ?) b ON c.id = b.category_id "
                +
                // 自分の取引実績を結合
                "LEFT JOIN (SELECT category_id, SUM(amount) AS total_spent FROM transactions WHERE strftime('%Y-%m', date) = ? AND type = 'EXPENSE' AND user_id = ? GROUP BY category_id) t ON c.id = t.category_id "
                +
                "WHERE c.type = 'EXPENSE' AND c.user_id = ? " + // ★ 他人のカテゴリを除外
                "ORDER BY c.name";

        try (Connection conn = DBManager.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, yearMonth);
            pstmt.setInt(2, userId); // 予算テーブルの絞り込み
            pstmt.setString(3, yearMonth);
            pstmt.setInt(4, userId); // 取引テーブルの絞り込み
            pstmt.setInt(5, userId); // カテゴリテーブルの絞り込み

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
     */
    public boolean saveOrUpdateBudget(String yearMonth, int categoryId, double amount, int userId) {
        // ★ user_id を追加
        // INSERT OR REPLACE は UNIQUE(user_id, year_month, category_id) 制約に基づいて動作します
        String sql = "INSERT OR REPLACE INTO budgets (id, user_id, year_month, category_id, amount) " +
                "VALUES ((SELECT id FROM budgets WHERE user_id = ? AND year_month = ? AND category_id = ?), ?, ?, ?, ?)";

        try (Connection conn = DBManager.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // サブクエリ用パラメータ
            pstmt.setInt(1, userId);
            pstmt.setString(2, yearMonth);
            pstmt.setInt(3, categoryId);

            // INSERT用パラメータ
            pstmt.setInt(4, userId); // ★ userId
            pstmt.setString(5, yearMonth);
            pstmt.setInt(6, categoryId);
            pstmt.setDouble(7, amount);

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("予算保存エラー: " + e.getMessage());
            return false;
        }
    }
}
