package com.example.CashFlowWeb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // --- 依存関係の定義 ---

    private final UserDAO userDAO;
    private final PasswordEncoder passwordEncoder;

    /**
     * コンストラクタ (依存性の注入)
     */
    @Autowired
    public AuthController(PasswordEncoder passwordEncoder) {
        this.userDAO = new UserDAO();
        this.passwordEncoder = passwordEncoder;
    }

    // --- 内部で使用するリクエスト用クラス ---

    private static class RegisterRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    // --- APIエンドポイント ---

    /**
     * ユーザー登録API (POST /api/auth/register)
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {

        String username = request.getUsername();
        String password = request.getPassword();

        // 1. ユーザー名が既に使われていないかチェック
        if (userDAO.findByUsername(username) != null) {
            return ResponseEntity.status(400).body("このユーザー名は既に使用されています。");
        }

        // 2. パスワードの入力チェック (空っぽかどうか)
        if (password == null || password.isEmpty()) {
            return ResponseEntity.status(400).body("パスワードが必要です。");
        }

        // ▼▼▼▼▼▼▼▼▼ ここから追加したセキュリティ強化部分 ▼▼▼▼▼▼▼▼▼

        // 3. パスワードの文字数チェック (8文字未満ならNG)
        if (password.length() < 8) {
            return ResponseEntity.status(400).body("パスワードは8文字以上で設定してください。");
        }

        // 4. 英数字混在チェック (正規表現)
        // (?=.*[0-9]) : 数字が1つ以上あるか
        // (?=.*[a-zA-Z]) : 英字が1つ以上あるか
        if (!password.matches("^(?=.*[0-9])(?=.*[a-zA-Z]).+$")) {
            return ResponseEntity.status(400).body("パスワードには英字と数字の両方を含めてください。");
        }

        // ▲▲▲▲▲▲▲▲▲ ここまで追加 ▲▲▲▲▲▲▲▲▲

        // 5. パスワードをハッシュ化 (【重要】)
        String hashedPassword = passwordEncoder.encode(password);

        // 6. 新しいUserオブジェクトを作成
        User newUser = new User(username, hashedPassword);
        newUser.setRole("USER"); // デフォルトロール

        // 7. データベースに保存
        boolean success = userDAO.saveUser(newUser);

        if (success) {
            return ResponseEntity.ok().body("ユーザー登録が成功しました。");
        } else {
            return ResponseEntity.status(500).body("サーバーエラーにより登録に失敗しました。");
        }
    }
}