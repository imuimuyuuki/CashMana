package com.example.CashFlowWeb;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryDAO {

    private static Map<Integer, Category> categoryCache = new HashMap<>();

    public void initializeCache() {
        categoryCache.clear();
        ensureSpecialCategoryExists("貯金", "EXPENSE");
        List<Category> allCategories = fetchAllCategoriesFromDb();
        allCategories.forEach(c -> categoryCache.put(c.getId(), c));
    }

    public Category getCategoryById(int id) {
        if (categoryCache.isEmpty())
            initializeCache();
        return categoryCache.getOrDefault(id, fetchCategoryByIdFromDb(id));
    }

    public List<Category> getAllCategories() {
        if (categoryCache.isEmpty())
            initializeCache();
        return new ArrayList<>(categoryCache.values());
    }

    public boolean addCategory(String name, String type) {
        String sql = "INSERT INTO categories(name, type) VALUES(?, ?)";
        try (Connection conn = DBManager.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, type);
            pstmt.executeUpdate();
            initializeCache();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean updateCategory(int id, String name, String type) {
        String sql = "UPDATE categories SET name = ?, type = ? WHERE id = ?";
        try (Connection conn = DBManager.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, type);
            pstmt.setInt(3, id);
            pstmt.executeUpdate();
            initializeCache();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean deleteCategory(int id) {
        String sql = "DELETE FROM categories WHERE id = ?";
        try (Connection conn = DBManager.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            if (pstmt.executeUpdate() > 0) {
                initializeCache();
                return true;
            }
            return false;
        } catch (SQLException e) {
            return false;
        }
    }

    private void ensureSpecialCategoryExists(String name, String type) {
        // 省略可ですが念のため実装
        String checkSql = "SELECT COUNT(*) FROM categories WHERE name = ? AND type = ?";
        try (Connection conn = DBManager.connect(); PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, type);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                addCategory(name, type);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<Category> fetchAllCategoriesFromDb() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM categories";
        try (Connection conn = DBManager.connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next())
                list.add(new Category(rs.getInt("id"), rs.getString("name"), rs.getString("type")));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Category fetchCategoryByIdFromDb(int id) {
        String sql = "SELECT * FROM categories WHERE id = ?";
        try (Connection conn = DBManager.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next())
                return new Category(rs.getInt("id"), rs.getString("name"), rs.getString("type"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}