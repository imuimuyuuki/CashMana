package com.example.CashFlowWeb;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionDAO {
    
    public boolean addTransaction(LocalDate date, double amount, String type, int categoryId, boolean isFuture, boolean isExtraordinary) {
        String sql = "INSERT INTO transactions(date, amount, type, category_id, is_future, is_extraordinary) VALUES(?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, date.toString());
            pstmt.setDouble(2, amount);
            pstmt.setString(3, type.toUpperCase());
            pstmt.setInt(4, categoryId);
            pstmt.setBoolean(5, isFuture);
            pstmt.setBoolean(6, isExtraordinary); 
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("データ登録エラー: " + e.getMessage());
            return false;
        }
    }
    
    public List<Transaction> getAllTransactions() {
        return getFilteredTransactions(null, null, null, null);
    }

    public List<Transaction> getFilteredTransactions(LocalDate startDate, LocalDate endDate, Integer categoryId, String type) {
        List<Transaction> transactions = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT t.id, t.date, t.amount, t.type, t.category_id, c.name AS category_name, t.is_future, t.is_extraordinary " +
            "FROM transactions t JOIN categories c ON t.category_id = c.id WHERE 1=1"
        );
        
        List<Object> params = new ArrayList<>();

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
        if (type != null && !type.trim().isEmpty()) {
            sql.append(" AND t.type = ?");
            params.add(type.toUpperCase());
        }

        sql.append(" ORDER BY t.date DESC, t.id DESC");

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
                        rs.getBoolean("is_extraordinary")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("データ取得エラー: " + e.getMessage());
        }
        return transactions;
    }

    public boolean updateTransaction(int id, LocalDate date, double amount, String type, int categoryId, boolean isFuture, boolean isExtraordinary) {
        String sql = "UPDATE transactions SET date = ?, amount = ?, type = ?, category_id = ?, is_future = ?, is_extraordinary = ? WHERE id = ?";
        try (Connection conn = DBManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, date.toString());
            pstmt.setDouble(2, amount);
            pstmt.setString(3, type.toUpperCase());
            pstmt.setInt(4, categoryId);
            pstmt.setBoolean(5, isFuture);
            pstmt.setBoolean(6, isExtraordinary);
            pstmt.setInt(7, id);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("データ更新エラー: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteTransaction(int id) {
        String sql = "DELETE FROM transactions WHERE id = ?";
        try (Connection conn = DBManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("データ削除エラー: " + e.getMessage());
            return false;
        }
    }
    
    public double calculateCurrentBalance() {
        String sql = "SELECT " +
                     "  COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END), 0) - " +
                     "  COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END), 0) " +
                     "FROM transactions " +
                     "WHERE is_future = FALSE";
        try (Connection conn = DBManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("残高計算エラー: " + e.getMessage());
        }
        return 0.0;
    }
    
    public List<CategorySummary> getCategorySummary(LocalDate startDate, LocalDate endDate, String type) {
        List<CategorySummary> summaryList = new ArrayList<>();
        String sql = "SELECT c.name AS category_name, SUM(t.amount) AS total_amount " +
                     "FROM transactions t " +
                     "JOIN categories c ON t.category_id = c.id " +
                     "WHERE t.date BETWEEN ? AND ? AND t.type = ? AND t.is_future = FALSE " +
                     "GROUP BY c.name " +
                     "ORDER BY total_amount DESC";

        try (Connection conn = DBManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, startDate.toString());
            pstmt.setString(2, endDate.toString());
            pstmt.setString(3, type.toUpperCase());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    summaryList.add(new CategorySummary(
                        rs.getString("category_name"),
                        rs.getDouble("total_amount")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("カテゴリ別集計エラー: " + e.getMessage());
        }
        return summaryList;
    }
    
    public List<MonthlySummary> getMonthlySummary() {
        List<MonthlySummary> summaries = new ArrayList<>();
        String sql = "SELECT " +
                     "  strftime('%Y-%m', date) AS month, " +
                     "  SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) AS totalIncome, " +
                     "  SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) AS totalExpense " +
                     "FROM transactions " +
                     "WHERE is_future = FALSE " +
                     "GROUP BY month " +
                     "ORDER BY month DESC";

        try (Connection conn = DBManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                summaries.add(new MonthlySummary(
                    rs.getString("month"),
                    rs.getDouble("totalIncome"),
                    rs.getDouble("totalExpense")
                ));
            }
        } catch (SQLException e) {
            System.err.println("月次集計エラー: " + e.getMessage());
        }
        return summaries;
    }
    
    public List<Double> getPastThreeMonthsRegularNetProfits() {
        String sql = "SELECT strftime('%Y-%m', date) AS month, " +
                     "SUM(CASE WHEN type = 'INCOME' THEN amount ELSE -amount END) AS net_profit " +
                     "FROM transactions " +
                     "WHERE is_future = FALSE AND is_extraordinary = FALSE " +
                     "GROUP BY month " +
                     "ORDER BY month DESC " +
                     "LIMIT 3";
        
        List<Double> netProfits = new ArrayList<>();

        try (Connection conn = DBManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                netProfits.add(rs.getDouble("net_profit"));
            }
        } catch (SQLException e) {
            System.err.println("過去3ヶ月純利益取得エラー: " + e.getMessage());
        }
        return netProfits.stream()
                         .collect(Collectors.collectingAndThen(Collectors.toList(), lst -> {
                             java.util.Collections.reverse(lst);
                             return lst;
                         }));
    }
    /**
     * IDを指定して単一の取引データを取得します。
     * @param id 取引ID
     * @return 見つかった取引データ。見つからなければnull。
     */
    public Transaction getTransactionById(int id) {
        String sql = "SELECT t.id, t.date, t.amount, t.type, t.category_id, c.name AS category_name, t.is_future, t.is_extraordinary " +
                     "FROM transactions t " +
                     "JOIN categories c ON t.category_id = c.id " +
                     "WHERE t.id = ?";

        try (Connection conn = DBManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);

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
                        rs.getBoolean("is_extraordinary")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("単一取引の取得エラー: " + e.getMessage());
        }
        return null; // データが見つからない場合
    }
}