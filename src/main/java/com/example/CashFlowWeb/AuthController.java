package com.example.CashFlowWeb;

import org.springframework.beans.factory.annotation.Autowired; // 依存性注入のために追加
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder; // パスワードハッシュ化のために追加
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    // 以前の簡易ログインロジックは削除 (Spring Securityが担当するため)

    // --- 依存関係の定義 ---

    private final UserDAO userDAO;
    private final PasswordEncoder passwordEncoder; // SecurityConfigで定義したBean

    /**
     * コンストラクタ (依存性の注入)
     * SpringがSecurityConfigで定義したPasswordEncoderを自動的に注入(DI)します。
     */
    @Autowired
    public AuthController(PasswordEncoder passwordEncoder) {
        this.userDAO = new UserDAO();
        this.passwordEncoder = passwordEncoder;
    }

    // --- 内部で使用するリクエスト用クラス ---
    
    /**
     * 登録リクエストのJSONを受け取るためのインナークラス
     * (register.htmlのJavaScriptから送信されるJSONに対応)
     */
    private static class RegisterRequest {
        private String username;
        private String password;

        // GetterとSetter (JSONデシリアライズに必須)
        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public void setUsername(String username) { this.username = username; }
        public void setPassword(String password) { this.password = password; }
    }

    // --- APIエンドポイント ---

    /**
     * ユーザー登録API (POST /api/auth/register)
     * register.htmlから呼び出されます。
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        
        // 1. ユーザー名が既に使われていないかチェック
        if (userDAO.findByUsername(request.getUsername()) != null) {
            return ResponseEntity.status(400).body("このユーザー名は既に使用されています。");
        }
        
        // 2. パスワードが空でないかチェック (簡易的)
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            return ResponseEntity.status(400).body("パスワードが必要です。");
        }

        // 3. パスワードをハッシュ化 (【重要】)
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // 4. 新しいUserオブジェクトを作成
        // (パスワードにはハッシュ化済みのものを設定)
        User newUser = new User(request.getUsername(), hashedPassword);
        newUser.setRole("USER"); // デフォルトロール

        // 5. データベースに保存
        boolean success = userDAO.saveUser(newUser);

        if (success) {
            return ResponseEntity.ok().body("ユーザー登録が成功しました。");
        } else {
            return ResponseEntity.status(500).body("サーバーエラーにより登録に失敗しました。");
        }
    }

    /*
     * 補足: /api/auth/login エンドポイントについて
     * SecurityConfig.java で .formLogin() を使用しているため、
     * ログイン処理 (POST /login) はSpring Securityが自動的に行います。
     * したがって、AuthController.java に /login API を手動で実装する必要はありません。
     * Spring Securityがログイン処理を行うには、次のステップで「UserDetailsService」の
     * 実装が必要になります。
     */
}
