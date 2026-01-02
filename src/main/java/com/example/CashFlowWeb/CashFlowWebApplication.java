package com.example.CashFlowWeb;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CashFlowWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(CashFlowWebApplication.class, args);
	}

	/**
	 * アプリケーション起動時に一度だけ実行される初期化処理。
	 * ここでデータベースのテーブル作成と、カテゴリのキャッシュ読み込みを行います。
	 */
	@Bean
	public CommandLineRunner initializeDbAndCache() {
		return args -> {
			System.out.println("--- データベース初期化とカテゴリキャッシュ開始 ---");

			// 1. データベース（テーブル）の作成
			DBManager.initializeDatabase();

			// 2. カテゴリキャッシュの初期化
			new CategoryDAO().initializeCache();

			System.out.println("--- データベース初期化とカテゴリキャッシュ完了 ---");
		};
	}
}
