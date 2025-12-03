package com.example.CashFlowWeb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserDAO userDAO;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthController(PasswordEncoder passwordEncoder) {
        this.userDAO = new UserDAO();
        this.passwordEncoder = passwordEncoder;
    }

    // 登録リクエスト用クラス
    private static class RegisterRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        if (userDAO.findByUsername(request.getUsername()) != null) {
            return ResponseEntity.status(400).body("このユーザー名は既に使用されています。");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            return ResponseEntity.status(400).body("パスワードが必要です。");
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User newUser = new User(request.getUsername(), hashedPassword);
        newUser.setRole("USER");

        boolean success = userDAO.saveUser(newUser);

        if (success) {
            return ResponseEntity.ok().body("ユーザー登録が成功しました。");
        } else {
            return ResponseEntity.status(500).body("サーバーエラーにより登録に失敗しました。");
        }
    }
}