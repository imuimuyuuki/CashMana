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

            // ユーザーテーブル
            String sqlUsers = "CREATE TABLE IF NOT EXISTS users (" +
                              "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                              "username TEXT NOT NULL UNIQUE, " +
                              "password_hash TEXT NOT NULL, " +
                              "role TEXT NOT NULL DEFAULT 'USER'" +
                              ");";
            stmt.execute(sqlUsers);

            // カテゴリテーブル
            String sqlCategory = "CREATE TABLE IF NOT EXISTS categories (" +
                                 "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                 "user_id INTEGER NOT NULL," +
                                 "name TEXT NOT NULL," +
                                 "type TEXT NOT NULL CHECK(type IN ('INCOME', 'EXPENSE'))," +
                                 "FOREIGN KEY (user_id) REFERENCES users(id)" +
                                 ");";
            stmt.execute(sqlCategory);

            // 目標テーブル (先に作成が必要)
            String sqlGoals = "CREATE TABLE IF NOT EXISTS goals (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "user_id INTEGER NOT NULL, " +
                         "name TEXT NOT NULL, " +
                         "target_amount REAL NOT NULL, " +
                         "current_amount REAL NOT NULL DEFAULT 0, " +
                         "target_date TEXT, " +
                         "image_url TEXT, " +
                         "FOREIGN KEY (user_id) REFERENCES users(id)" +
                         ")";
            stmt.execute(sqlGoals);

            // 取引テーブル (goal_id を追加)
            String sqlTransaction = "CREATE TABLE IF NOT EXISTS transactions (" +
                                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                    "user_id INTEGER NOT NULL," +
                                    "date TEXT NOT NULL," +
                                    "amount REAL NOT NULL," +
                                    "type TEXT NOT NULL CHECK(type IN ('INCOME', 'EXPENSE'))," +
                                    "category_id INTEGER," +
                                    "goal_id INTEGER," + // ★追加: どの目標への入金か
                                    "is_future BOOLEAN NOT NULL DEFAULT FALSE," +
                                    "is_extraordinary BOOLEAN NOT NULL DEFAULT FALSE," +
                                    "FOREIGN KEY (category_id) REFERENCES categories(id)," +
                                    "FOREIGN KEY (goal_id) REFERENCES goals(id)," + // ★追加
                                    "FOREIGN KEY (user_id) REFERENCES users(id)" +
                                    ");";
            stmt.execute(sqlTransaction);

            // 資産テーブル
            String sqlAssets = "CREATE TABLE IF NOT EXISTS assets (" +
                               "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                               "user_id INTEGER NOT NULL," +
                               "name TEXT NOT NULL," +
                               "ticker_symbol TEXT," +
                               "quantity REAL NOT NULL," +
                               "purchase_price REAL NOT NULL," +
                               "current_price REAL NOT NULL," +
                               "asset_type TEXT," +
                               "FOREIGN KEY (user_id) REFERENCES users(id)" +
                               ");";
            stmt.execute(sqlAssets);

            // 予算テーブル
            String sqlBudgets = "CREATE TABLE IF NOT EXISTS budgets (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                "user_id INTEGER NOT NULL," +
                                "year_month TEXT NOT NULL," +
                                "category_id INTEGER NOT NULL," +
                                "amount REAL NOT NULL," +
                                "UNIQUE(user_id, year_month, category_id)," +
                                "FOREIGN KEY (category_id) REFERENCES categories(id)," +
                                "FOREIGN KEY (user_id) REFERENCES users(id)" +
                                ");";
            stmt.execute(sqlBudgets);
            
        } catch (SQLException e) {
            System.err.println("データベース初期化エラー: " + e.getMessage());
        }
    }
}
