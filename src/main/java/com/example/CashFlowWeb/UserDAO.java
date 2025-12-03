package com.example.CashFlowWeb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.stereotype.Repository;

@Repository
public class UserDAO {

    public User findByUsername(String username) {
        String sql = "SELECT id, username, password_hash, role FROM users WHERE LOWER(username) = LOWER(?)";

        try (Connection conn = DBManager.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password_hash"));
                    user.setRole(rs.getString("role"));
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("ユーザー検索エラー: " + e.getMessage());
        }
        return null;
    }

    public boolean saveUser(User user) {
        String sql = "INSERT INTO users(username, password_hash, role) VALUES(?, ?, ?)";

        try (Connection conn = DBManager.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole());

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("ユーザー保存エラー: " + e.getMessage());
            return false;
        }
    }
}