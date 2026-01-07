package com.example.CashFlowWeb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBManager {

    private static final String URL = "jdbc:sqlite:cashflow.db";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = connect();
                Statement stmt = conn.createStatement()) {

            // ユーザーテーブル (一番最初に作成する必要があります)
            String sqlUsers = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT NOT NULL UNIQUE, " +
                    "password_hash TEXT NOT NULL, " +
                    "role TEXT NOT NULL DEFAULT 'USER'" +
                    ");";
            stmt.execute(sqlUsers);

            // 1. カテゴリテーブル (user_idを追加)
            String sqlCategory = "CREATE TABLE IF NOT EXISTS categories (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," + // ★追加
                    "name TEXT NOT NULL," +
                    "type TEXT NOT NULL CHECK(type IN ('INCOME', 'EXPENSE'))," +
                    "FOREIGN KEY (user_id) REFERENCES users(id)" + // ★追加
                    ");";
            stmt.execute(sqlCategory);

            // 2. 取引テーブル (user_idを追加)
            String sqlTransaction = "CREATE TABLE IF NOT EXISTS transactions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," + // ★追加
                    "date TEXT NOT NULL," +
                    "amount REAL NOT NULL," +
                    "type TEXT NOT NULL CHECK(type IN ('INCOME', 'EXPENSE'))," +
                    "category_id INTEGER," +
                    "is_future BOOLEAN NOT NULL DEFAULT FALSE," +
                    "is_extraordinary BOOLEAN NOT NULL DEFAULT FALSE," + // 臨時収支フラグ
                    "FOREIGN KEY (category_id) REFERENCES categories(id)," +
                    "FOREIGN KEY (user_id) REFERENCES users(id)" + // ★追加
                    ");";
            stmt.execute(sqlTransaction);

            // 3. 資産テーブル (user_idを追加)
            String sqlAssets = "CREATE TABLE IF NOT EXISTS assets (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," + // ★追加
                    "name TEXT NOT NULL," +
                    "ticker_symbol TEXT," +
                    "quantity REAL NOT NULL," +
                    "purchase_price REAL NOT NULL," +
                    "current_price REAL NOT NULL," +
                    "asset_type TEXT," +
                    "FOREIGN KEY (user_id) REFERENCES users(id)" + // ★追加
                    ");";
            stmt.execute(sqlAssets);

            // 4. 予算テーブル (user_idを追加)
            String sqlBudgets = "CREATE TABLE IF NOT EXISTS budgets (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," + // ★追加
                    "year_month TEXT NOT NULL," +
                    "category_id INTEGER NOT NULL," +
                    "amount REAL NOT NULL," +
                    "UNIQUE(user_id, year_month, category_id)," + // ★制約変更
                    "FOREIGN KEY (category_id) REFERENCES categories(id)," +
                    "FOREIGN KEY (user_id) REFERENCES users(id)" + // ★追加
                    ");";
            stmt.execute(sqlBudgets);

            // 5. 目標テーブル (user_idを追加)
            stmt.execute("CREATE TABLE IF NOT EXISTS goals (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER NOT NULL, " + // ★追加
                    "name TEXT NOT NULL, " +
                    "target_amount REAL NOT NULL, " +
                    "current_amount REAL NOT NULL DEFAULT 0, " +
                    "target_date TEXT, " +
                    "image_url TEXT, " +
                    "FOREIGN KEY (user_id) REFERENCES users(id)" + // ★追加
                    ")");

        } catch (SQLException e) {
            System.err.println("データベース初期化エラー: " + e.getMessage());
        }
    }
}
