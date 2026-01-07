package com.example.CashFlowWeb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Securityがログイン認証を行うために使用するクラス。
 * UserDetailsServiceインターフェースを実装し、
 * ユーザー名からユーザー情報を取得する方法を定義します。
 */
@Service // 👈 このクラスをSpringのサービス（Bean）として登録する
public class CustomUserDetailsService implements UserDetailsService {

    // 依存性の注入 (DI)
    // Springが@Repositoryの付いたUserDAOのインスタンスを自動的にここにセットします。
    @Autowired
    private UserDAO userDAO;

    /**
     * Spring Securityがログイン処理（/login）を実行する際に、
     * ユーザー名（username）を引数としてこのメソッドを呼び出します。
     * * @param username ログインフォームで入力されたユーザー名
     * @return データベースから見つかったUserオブジェクト (UserDetails)
     * @throws UsernameNotFoundException ユーザーが見つからなかった場合にスロー
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        // 1. UserDAOを使って、DBからユーザー名で検索
        User user = userDAO.findByUsername(username);

        // 2. ユーザーが見つからなかった場合の処理
        if (user == null) {
            // この例外をスローすると、Spring Securityは認証失敗（Bad credentials）として処理します。
            throw new UsernameNotFoundException("ユーザーが見つかりません: " + username);
        }

        // 3. ユーザーが見つかった場合
        // User.java (モデル) は既に UserDetails インターフェースを実装しているため、
        // そのまま返すだけでSpring Securityがパスワードの比較などを行います。
        return user;
    }
}
