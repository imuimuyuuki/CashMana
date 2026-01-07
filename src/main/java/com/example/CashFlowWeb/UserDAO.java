package com.example.CashFlowWeb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// ▼▼▼ この1行を追加 ▼▼▼
import org.springframework.stereotype.Repository;
// ▲▲▲ この1行を追加 ▲▲▲

/**
 * ユーザー(users)テーブルのデータベース操作を担当するクラス。
 */
@Repository // 👈 1. このクラスをSpringのBeanとして登録する
public class UserDAO {

    /**
     * ユーザー名（username）を指定して、データベースからユーザー情報を検索します。
     * Spring Securityの認証処理（ログイン時）に使われます。
     *
     * @param username 検索するユーザー名
     * @return 見つかったUserオブジェクト。見つからなければnull。
     */
    public User findByUsername(String username) {
        // ユーザー名は大文字小文字を区別しない（toLowerCase()）
        String sql = "SELECT id, username, password_hash, role FROM users WHERE LOWER(username) = LOWER(?)";
        
        try (Connection conn = DBManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // DBから取得した情報をUserオブジェクトにマッピング
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password_hash")); // ハッシュ化されたパスワード
                    user.setRole(rs.getString("role"));
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("ユーザー検索エラー: " + e.getMessage());
        }
        return null; // ユーザーが見つからなかった場合
    }

    /**
     * 新しいユーザーをデータベースに保存します。
     * 登録API（AuthController）で使われます。
     *
     * @param user 保存するUserオブジェクト
     * @return 保存に成功した場合はtrue
     */
    public boolean saveUser(User user) {
        // パスワードは、AuthController側でハッシュ化されてから渡される想定
        String sql = "INSERT INTO users(username, password_hash, role) VALUES(?, ?, ?)";

        try (Connection conn = DBManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword()); // ハッシュ化済みパスワード
            pstmt.setString(3, user.getRole());

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("ユーザー保存エラー (ユーザー名が重複している可能性あり): " + e.getMessage());
            return false;
        }
    }
}
