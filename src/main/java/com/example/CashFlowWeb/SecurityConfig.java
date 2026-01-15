package com.example.CashFlowWeb;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // --- 認証ルールの設定 ---
            .authorizeHttpRequests(authorize -> authorize
                // 1. 【許可】ログイン前でもアクセス可能なパスを指定
                .requestMatchers(
                    "/login.html",        // ログインページ
                    "/register.html",     // 【★最重要】登録ページを許可
                    "/api/auth/register", // 【★最重要】登録APIを許可
                    "/style.css"          // CSS
                ).permitAll()
                
                // 2. 【認証必須】その他のすべてのリクエスト（index.htmlなど）は認証を必須にする
                .anyRequest().authenticated()
            )
            
            // --- ログインフォームの設定 ---
            .formLogin(form -> form
                .loginPage("/login.html") 
                .loginProcessingUrl("/login") // ログインフォームのPOST先
                .defaultSuccessUrl("/index.html", true) // ログイン成功後のリダイレクト先
                .permitAll() 
            )
            
            // --- ログアウトの設定 ---
            .logout(logout -> logout
                .logoutSuccessUrl("/login.html")
            )
            
            // --- CSRF設定 ---
            // (登録APIをPOSTで使うため無効化)
            .csrf(csrf -> csrf.disable());

        return http.build();
    }

    /**
     * パスワードをハッシュ化するためのエンコーダー
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
