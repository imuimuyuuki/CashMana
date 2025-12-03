package com.example.CashFlowWeb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBManager {

    // SQLite用の接続URL
    private static final String URL = "jdbc:sqlite:cashflow.db";

    public static Connection connect() throws SQLException {
        try {
            // SQLiteドライバーのロード
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC Driver not found.", e);
        }
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = connect();
                Statement stmt = conn.createStatement()) {

            // 外部キー制約を有効化 (SQLiteではデフォルト無効のため)
            stmt.execute("PRAGMA foreign_keys = ON;");

            // 1. ユーザーテーブル
            String sqlUsers = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT NOT NULL UNIQUE, " +
                    "password_hash TEXT NOT NULL, " +
                    "role TEXT NOT NULL DEFAULT 'USER'" +
                    ");";
            stmt.execute(sqlUsers);

            // 2. カテゴリテーブル
            String sqlCategory = "CREATE TABLE IF NOT EXISTS categories (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL," +
                    "type TEXT NOT NULL CHECK(type IN ('INCOME', 'EXPENSE'))" +
                    ");";
            stmt.execute(sqlCategory);

            // 3. 取引テーブル (user_idを追加)
            String sqlTransaction = "CREATE TABLE IF NOT EXISTS transactions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," + // 追加
                    "date TEXT NOT NULL," +
                    "amount REAL NOT NULL," +
                    "type TEXT NOT NULL CHECK(type IN ('INCOME', 'EXPENSE'))," +
                    "category_id INTEGER," +
                    "is_future BOOLEAN NOT NULL DEFAULT FALSE," +
                    "is_extraordinary BOOLEAN NOT NULL DEFAULT FALSE," +
                    "FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL," +
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                    ");";
            stmt.execute(sqlTransaction);

            // 4. 資産テーブル (user_idを追加)
            String sqlAssets = "CREATE TABLE IF NOT EXISTS assets (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER NOT NULL," + // 追加
                    "name TEXT NOT NULL, " +
                    "ticker_symbol TEXT," +
                    "quantity REAL NOT NULL, " +
                    "purchase_price REAL NOT NULL, " +
                    "current_price REAL NOT NULL, " +
                    "asset_type TEXT," +
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                    ");";
            stmt.execute(sqlAssets);

            // 5. 予算テーブル (user_idを追加)
            String sqlBudgets = "CREATE TABLE IF NOT EXISTS budgets (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," + // 追加
                    "year_month TEXT NOT NULL," +
                    "category_id INTEGER NOT NULL," +
                    "amount REAL NOT NULL," +
                    "UNIQUE(user_id, year_month, category_id)," + // ユーザーごとにユニーク
                    "FOREIGN KEY (category_id) REFERENCES categories(id)," +
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                    ");";
            stmt.execute(sqlBudgets);

            // 6. 目標テーブル (user_idを追加)
            stmt.execute("CREATE TABLE IF NOT EXISTS goals (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER NOT NULL," + // 追加
                    "name TEXT NOT NULL, " +
                    "target_amount REAL NOT NULL, " +
                    "current_amount REAL NOT NULL DEFAULT 0, " +
                    "target_date TEXT, " +
                    "image_url TEXT," +
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)");

            System.out.println("SQLiteデータベース初期化完了 (マルチユーザー対応)");

        } catch (SQLException e) {
            System.err.println("データベース初期化エラー: " + e.getMessage());
        }
    }
}