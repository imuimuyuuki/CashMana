package com.example.CashFlowWeb;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AssetDAO {

    public boolean addAsset(Asset asset, int userId) {
        String sql = "INSERT INTO assets(user_id, name, ticker_symbol, quantity, purchase_price, current_price, asset_type) VALUES(?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBManager.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
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

    public List<Asset> getAllAssets(int userId) {
        List<Asset> assets = new ArrayList<>();
        String sql = "SELECT * FROM assets WHERE user_id = ?";
        try (Connection conn = DBManager.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    assets.add(mapToAsset(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return assets;
    }

    public Asset getAssetById(int id, int userId) {
        String sql = "SELECT * FROM assets WHERE id = ? AND user_id = ?";
        try (Connection conn = DBManager.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setInt(2, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    return mapToAsset(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateAsset(Asset asset, int userId) {
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
            pstmt.setInt(8, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean deleteAsset(int id, int userId) {
        String sql = "DELETE FROM assets WHERE id = ? AND user_id = ?";
        try (Connection conn = DBManager.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    private Asset mapToAsset(ResultSet rs) throws SQLException {
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