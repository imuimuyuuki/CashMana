package com.example.CashFlowWeb;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    // ※今回はシンプル化とバグ防止のため、メモリキャッシュ(Map)を廃止し、常にDBを参照します。

    /**
     * すべてのカテゴリのリストを取得します。
     * ★カテゴリが1つもない場合は、初期カテゴリ（給料、食費など）を自動作成します。
     */
    public List<Category> getAllCategories(int userId) {
        // 1. まず、このユーザーのカテゴリが存在するかチェック
        if (!hasCategories(userId)) {
            createDefaultCategories(userId);
        }

        List<Category> categories = new ArrayList<>();
        // ★ user_id でフィルタリング
        String sql = "SELECT id, name, type FROM categories WHERE user_id = ? ORDER BY type, name";

        try (Connection conn = DBManager.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    categories.add(new Category(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("type")));
                }
            }
        } catch (SQLException e) {
            System.err.println("カテゴリ取得エラー: " + e.getMessage());
        }
        return categories;
    }

    /**
     * ユーザーがカテゴリを持っているか確認します。
     */
    private boolean hasCategories(int userId) {
        String sql = "SELECT COUNT(*) FROM categories WHERE user_id = ?";
        try (Connection conn = DBManager.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * ★新規ユーザー用のデフォルトカテゴリを作成します。
     */
    private void createDefaultCategories(int userId) {
        System.out.println("新規ユーザー(ID:" + userId + ")用のデフォルトカテゴリを作成します...");

        // デフォルトのカテゴリリスト
        String[][] defaults = {
                { "給料", "INCOME" },
                { "ボーナス", "INCOME" },
                { "副業", "INCOME" },
                { "食費", "EXPENSE" },
                { "日用品", "EXPENSE" },
                { "交通費", "EXPENSE" },
                { "家賃", "EXPENSE" },
                { "通信費", "EXPENSE" },
                { "娯楽", "EXPENSE" },
                { "貯金", "EXPENSE" } // ※貯金カテゴリはシステム上重要
        };

        String sql = "INSERT INTO categories(user_id, name, type) VALUES(?, ?, ?)";

        try (Connection conn = DBManager.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false); // 一括登録のためトランザクション開始

            for (String[] cat : defaults) {
                pstmt.setInt(1, userId);
                pstmt.setString(2, cat[0]);
                pstmt.setString(3, cat[1]);
                pstmt.addBatch();
            }

            pstmt.executeBatch();
            conn.commit(); // コミット

        } catch (SQLException e) {
            System.err.println("デフォルトカテゴリ作成エラー: " + e.getMessage());
        }
    }

    public boolean addCategory(String name, String type, int userId) {
        String sql = "INSERT INTO categories(user_id, name, type) VALUES(?, ?, ?)";
        try (Connection conn = DBManager.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, name);
            pstmt.setString(3, type);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("カテゴリ追加エラー: " + e.getMessage());
            return false;
        }
    }

    public boolean updateCategory(int id, String name, String type, int userId) {
        String sql = "UPDATE categories SET name = ?, type = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = DBManager.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, type);
            pstmt.setInt(3, id);
            pstmt.setInt(4, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteCategory(int id, int userId) {
        if (isCategoryUsed(id)) {
            System.err.println("カテゴリ削除エラー: 関連取引が存在するため削除できません。");
            return false;
        }

        String sql = "DELETE FROM categories WHERE id = ? AND user_id = ?";
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

    // ※単純化のため userId チェックは省略していますが、transaction側で整合性が取れていれば問題ありません
    private boolean isCategoryUsed(int categoryId) {
        String sql = "SELECT COUNT(*) FROM transactions WHERE category_id = ?";
        try (Connection conn = DBManager.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, categoryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
