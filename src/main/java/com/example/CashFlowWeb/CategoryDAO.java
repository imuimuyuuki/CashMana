package com.example.CashFlowWeb;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryDAO {

    // キャッシュ: IDからカテゴリ名を取得する際の効率化
    private Map<Integer, Category> categoryCache = new HashMap<>();

    /**
     * キャッシュの初期化（「貯金」カテゴリの存在確認も行う）
     */
    public void initializeCache() {
        categoryCache.clear();
        
        // 【重要】「貯金」という支出カテゴリが存在するか確認し、なければ作成
        ensureSpecialCategoryExists("貯金", "EXPENSE");
        
        // 全カテゴリをDBから取得してキャッシュを構築
        List<Category> allCategories = fetchAllCategoriesFromDb();
        allCategories.forEach(c -> categoryCache.put(c.getId(), c));
    }

    /**
     * IDに基づいて単一のカテゴリを取得します。（キャッシュ優先）
     */
    public Category getCategoryById(int id) {
        if (categoryCache.isEmpty()) {
            initializeCache();
        }
        // キャッシュに存在しない場合はDBから取得 (稀なケース)
        return categoryCache.getOrDefault(id, fetchCategoryByIdFromDb(id));
    }
    
    /**
     * すべてのカテゴリのリストを取得します。（キャッシュ優先）
     */
    public List<Category> getAllCategories() {
        if (categoryCache.isEmpty()) {
            initializeCache();
        }
        // DBからではなくキャッシュからリストを生成して返す
        return new ArrayList<>(categoryCache.values());
    }

    /**
     * (キャッシュミス時の) DBからIDに基づいて単一のカテゴリを取得します。
     * 【修正点】Category(id, name, type) コンストラクタを使用
     */
    private Category fetchCategoryByIdFromDb(int id) {
        String sql = "SELECT id, name, type FROM categories WHERE id = ?";
        try (Connection conn = DBManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Category(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("type")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("DBからのカテゴリ取得エラー: " + e.getMessage());
        }
        return null;
    }

    /**
     * DBからすべてのカテゴリを取得します。(キャッシュ構築用)
     * 【修正点】Category(id, name, type) コンストラクタを使用
     */
    private List<Category> fetchAllCategoriesFromDb() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT id, name, type FROM categories ORDER BY type, name";
        try (Connection conn = DBManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                categories.add(new Category(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("type")
                ));
            }
        } catch (SQLException e) {
            System.err.println("全カテゴリ取得エラー: " + e.getMessage());
        }
        return categories;
    }

    /**
     * カテゴリを追加し、キャッシュを更新します。
     */
    public boolean addCategory(String name, String type) {
        boolean success = internalAddCategory(name, type);
        if (success) {
            initializeCache(); // キャッシュを更新
        }
        return success;
    }

    /**
     * 【新規追加】カテゴリを追加する内部メソッド（キャッシュの更新は行わない）
     */
    private boolean internalAddCategory(String name, String type) {
        String sql = "INSERT INTO categories(name, type) VALUES(?, ?)";
        try (Connection conn = DBManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name.trim());
            pstmt.setString(2, type.toUpperCase());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("カテゴリ登録エラー: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 【新規追加】特定の名前とタイプのカテゴリが存在するか確認し、なければ作成します。
     */
    private void ensureSpecialCategoryExists(String name, String type) {
        String checkSql = "SELECT COUNT(*) FROM categories WHERE name = ? AND type = ?";
        try (Connection conn = DBManager.connect();
             PreparedStatement pstmtCheck = conn.prepareStatement(checkSql)) {
            
            pstmtCheck.setString(1, name);
            pstmtCheck.setString(2, type);
            
            try (ResultSet rs = pstmtCheck.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    // カテゴリが存在しない場合、追加する
                    internalAddCategory(name, type); 
                    System.out.println("「" + name + "」カテゴリを自動作成しました。");
                }
            }
        } catch (SQLException e) {
            System.err.println("特殊カテゴリの確認エラー: " + e.getMessage());
        }
    }

    /**
     * 既存のカテゴリを更新します。
     */
    public boolean updateCategory(int id, String name, String type) {
        String sql = "UPDATE categories SET name = ?, type = ? WHERE id = ?";
        try (Connection conn = DBManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name.trim());
            pstmt.setString(2, type.toUpperCase());
            pstmt.setInt(3, id);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                initializeCache(); // キャッシュを更新
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("カテゴリ更新エラー: " + e.getMessage());
            return false;
        }
    }

    /**
     * カテゴリを削除します。
     */
    public boolean deleteCategory(int id) {
        // カテゴリに関連付けられた取引が存在するかチェック
        if (isCategoryUsed(id)) {
            System.err.println("カテゴリ削除エラー: 関連取引が存在するため削除できません。");
            return false;
        }

        String sql = "DELETE FROM categories WHERE id = ?";
        try (Connection conn = DBManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                initializeCache(); // キャッシュを更新
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("カテゴリ削除エラー: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * カテゴリが取引で使われているか確認
     */
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
            System.err.println("カテゴリ使用状況チェックエラー: " + e.getMessage());
        }
        return false;
    }
}