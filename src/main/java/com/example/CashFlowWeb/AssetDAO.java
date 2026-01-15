package com.example.CashFlowWeb;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 資産(assets)テーブルのデータベース操作を担当するクラス。
 */
public class AssetDAO {

    /**
     * 新しい資産を登録します (Create)。
     */
    public boolean addAsset(Asset asset, int userId) {
        // ★ user_id を追加
        String sql = "INSERT INTO assets(user_id, name, ticker_symbol, quantity, purchase_price, current_price, asset_type) VALUES(?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBManager.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId); // ★ ユーザーIDをセット
            pstmt.setString(2, asset.getName());
            pstmt.setString(3, asset.getTickerSymbol());
            pstmt.setDouble(4, asset.getQuantity());
            pstmt.setDouble(5, asset.getPurchasePrice());
            pstmt.setDouble(6, asset.getCurrentPrice());
            pstmt.setString(7, asset.getAssetType());

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("資産登録エラー: " + e.getMessage());
            return false;
        }
    }

    /**
     * 登録されているすべての資産を取得します (Read)。
     */
    public List<Asset> getAllAssets(int userId) {
        List<Asset> assets = new ArrayList<>();
        // ★ WHERE user_id = ? を追加
        String sql = "SELECT id, name, ticker_symbol, quantity, purchase_price, current_price, asset_type FROM assets WHERE user_id = ?";

        try (Connection conn = DBManager.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId); // ★ ユーザーIDをセット

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    assets.add(new Asset(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("ticker_symbol"),
                            rs.getDouble("quantity"),
                            rs.getDouble("purchase_price"),
                            rs.getDouble("current_price"),
                            rs.getString("asset_type")));
                }
            }
        } catch (SQLException e) {
            System.err.println("資産取得エラー: " + e.getMessage());
        }
        return assets;
    }

    /**
     * IDに基づいて単一の資産を取得します。
     */
    public Asset getAssetById(int id, int userId) {
        // ★ WHERE id = ? AND user_id = ? に変更
        String sql = "SELECT id, name, ticker_symbol, quantity, purchase_price, current_price, asset_type FROM assets WHERE id = ? AND user_id = ?";
        try (Connection conn = DBManager.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.setInt(2, userId); // ★ ユーザーID確認

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Asset(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("ticker_symbol"),
                            rs.getDouble("quantity"),
                            rs.getDouble("purchase_price"),
                            rs.getDouble("current_price"),
                            rs.getString("asset_type"));
                }
            }
        } catch (SQLException e) {
            System.err.println("単一資産取得エラー: " + e.getMessage());
        }
        return null;
    }

    /**
     * 既存の資産情報を更新します (Update)。
     */
    public boolean updateAsset(Asset asset, int userId) {
        // ★ user_id 条件を追加
        String sql = "UPDATE assets SET name = ?, ticker_symbol = ?, quantity = ?, purchase_price = ?, current_price = ?, asset_type = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = DBManager.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, asset.getName());
            pstmt.setString(2, asset.getTickerSymbol());
            pstmt.setDouble(3, asset.getQuantity());
            pstmt.setDouble(4, asset.getPurchasePrice());
            pstmt.setDouble(5, asset.getCurrentPrice());
            pstmt.setString(6, asset.getAssetType());
            pstmt.setInt(7, asset.getId());
            pstmt.setInt(8, userId); // ★ ユーザーID確認

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("資産更新エラー: " + e.getMessage());
            return false;
        }
    }

    /**
     * 指定されたIDの資産を削除します (Delete)。
     */
    public boolean deleteAsset(int id, int userId) {
        // ★ user_id 条件を追加
        String sql = "DELETE FROM assets WHERE id = ? AND user_id = ?";
        try (Connection conn = DBManager.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.setInt(2, userId); // ★ ユーザーID確認
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("資産削除エラー: " + e.getMessage());
            return false;
        }
    }
}
