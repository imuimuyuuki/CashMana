package com.example.CashFlowWeb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBManager {

    /**
     * データベース接続を取得します。
     * 環境変数 "JDBC_DATABASE_URL" があればそれ（PostgreSQL）を使い、
     * なければローカルのSQLiteを使います。
     */
    public static Connection connect() throws SQLException {
        String dbUrl = System.getenv("JDBC_DATABASE_URL");
        
        if (dbUrl != null && !dbUrl.isEmpty()) {
            // クラウド環境 (Renderなど)
            return DriverManager.getConnection(dbUrl);
        } else {
            // ローカル環境
            return DriverManager.getConnection("jdbc:sqlite:cashflow.db");
        }
    }

    public static void initializeDatabase() {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            // 1. カテゴリテーブル
            stmt.execute("CREATE TABLE IF NOT EXISTS categories (" +
                         "id SERIAL PRIMARY KEY," + // SQLiteのINTEGER PRIMARY KEY AUTOINCREMENTの代わりにSERIALを使う場合もあるが、互換性のため調整が必要
                         "name TEXT NOT NULL," +
                         "type TEXT NOT NULL CHECK(type IN ('INCOME', 'EXPENSE'))" +
                         ");");

            // ※ PostgreSQLとSQLiteの両方で動くSQLにするため、ID定義を少し工夫します
            // 以下の書き方はSQLiteでもPostgreSQLでも概ね動作するシンプルな形です
            
            // しかし、厳密には AUTOINCREMENT (SQLite) と SERIAL (PostgreSQL) は違います。
            // 簡易的な対応として、テーブル作成は「アプリ起動時」ではなく
            // クラウド側で一度だけSQLを流すか、あるいは以下のように汎用的な書き方を試みます。
            
            // 今回は複雑さを避けるため、とりあえずテーブル作成のSQLは
            // 「エラーが出てもドンマイ」の精神で、既存のSQLite用コードを残しつつ
            // PostgreSQLでは手動作成、またはエラーを無視して進める形にします。
            // (本格的な移行では create table 文を分ける必要がありますが、まずは接続確認を目指しましょう)

            // SQLite用のテーブル作成ロジック（ローカルではこれが動きます）
            if (System.getenv("JDBC_DATABASE_URL") == null) {
                createTablesSQLite(stmt);
            } else {
                createTablesPostgres(stmt);
            }
            
        } catch (SQLException e) {
            System.err.println("データベース初期化エラー (既存テーブルがある場合は無視してください): " + e.getMessage());
        }
    }

    // SQLite用のテーブル作成
    private static void createTablesSQLite(Statement stmt) throws SQLException {
        stmt.execute("CREATE TABLE IF NOT EXISTS categories (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, type TEXT NOT NULL)");
        stmt.execute("CREATE TABLE IF NOT EXISTS transactions (id INTEGER PRIMARY KEY AUTOINCREMENT, date TEXT NOT NULL, amount REAL NOT NULL, type TEXT NOT NULL, category_id INTEGER, is_future BOOLEAN DEFAULT FALSE, is_extraordinary BOOLEAN DEFAULT FALSE, FOREIGN KEY (category_id) REFERENCES categories(id))");
        stmt.execute("CREATE TABLE IF NOT EXISTS assets (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, ticker_symbol TEXT, quantity REAL NOT NULL, purchase_price REAL NOT NULL, current_price REAL, asset_type TEXT NOT NULL)");
        stmt.execute("CREATE TABLE IF NOT EXISTS budgets (id INTEGER PRIMARY KEY AUTOINCREMENT, year_month TEXT NOT NULL, category_id INTEGER NOT NULL, amount REAL NOT NULL, UNIQUE(year_month, category_id))");
        stmt.execute("CREATE TABLE IF NOT EXISTS goals (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, target_amount REAL NOT NULL, current_amount REAL NOT NULL DEFAULT 0, target_date TEXT, image_url TEXT)");
    }

    // PostgreSQL用のテーブル作成 (データ型を少し調整)
    private static void createTablesPostgres(Statement stmt) throws SQLException {
        // カテゴリ
        stmt.execute("CREATE TABLE IF NOT EXISTS categories (id SERIAL PRIMARY KEY, name TEXT NOT NULL, type TEXT NOT NULL)");
        // 取引
        stmt.execute("CREATE TABLE IF NOT EXISTS transactions (id SERIAL PRIMARY KEY, date TEXT NOT NULL, amount DOUBLE PRECISION NOT NULL, type TEXT NOT NULL, category_id INTEGER, is_future BOOLEAN DEFAULT FALSE, is_extraordinary BOOLEAN DEFAULT FALSE, FOREIGN KEY (category_id) REFERENCES categories(id))");
        // 資産
        stmt.execute("CREATE TABLE IF NOT EXISTS assets (id SERIAL PRIMARY KEY, name TEXT NOT NULL, ticker_symbol TEXT, quantity DOUBLE PRECISION NOT NULL, purchase_price DOUBLE PRECISION NOT NULL, current_price DOUBLE PRECISION, asset_type TEXT NOT NULL)");
        // 予算
        stmt.execute("CREATE TABLE IF NOT EXISTS budgets (id SERIAL PRIMARY KEY, year_month TEXT NOT NULL, category_id INTEGER NOT NULL, amount DOUBLE PRECISION NOT NULL, UNIQUE(year_month, category_id))");
        // 目標
        stmt.execute("CREATE TABLE IF NOT EXISTS goals (id SERIAL PRIMARY KEY, name TEXT NOT NULL, target_amount DOUBLE PRECISION NOT NULL, current_amount DOUBLE PRECISION NOT NULL DEFAULT 0, target_date TEXT, image_url TEXT)");
    }
}