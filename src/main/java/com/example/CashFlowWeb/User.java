package com.example.CashFlowWeb;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * ユーザー情報を保持するモデルクラス。
 * Spring SecurityのUserDetailsインターフェースを実装し、認証に利用できるようにします。
 */
public class User implements UserDetails {

    private int id;
    private String username;
    private String password; // データベースにはハッシュ化されたパスワードを保存します
    
    // Spring Security が要求する権限（今回は全員 "USER" 固定）
    private String role = "USER"; 

    // --- コンストラクタ ---
    public User() {}

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public User(int id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    // --- Getter / Setter ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @Override
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    @Override
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }


    // ========================================================================
    // UserDetails インターフェースの実装 (Spring Security に必須)
    // ========================================================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // ユーザーの権限（ロール）を返します。
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + this.role));
    }

    @Override
    public boolean isAccountNonExpired() {
        // アカウントの有効期限（今回は常に有効）
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // アカウントのロック状態（今回は常にロックなし）
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // 資格情報（パスワード）の有効期限（今回は常に有効）
        return true;
    }

    @Override
    public boolean isEnabled() {
        // アカウントの有効状態（今回は常に有効）
        return true;
    }
}
