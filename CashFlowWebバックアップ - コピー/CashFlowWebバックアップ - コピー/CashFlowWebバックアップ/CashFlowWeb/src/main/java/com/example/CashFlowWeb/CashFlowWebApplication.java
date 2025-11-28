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

	@Bean
	public CommandLineRunner initializeDbAndCache() {
		return args -> {
			System.out.println("--- データベース初期化とカテゴリキャッシュ開始 ---");
			DBManager.initializeDatabase(); // DBManagerを呼び出してテーブル作成
			new CategoryDAO().initializeCache(); // カテゴリキャッシュを初期化
			System.out.println("--- データベース初期化とカテゴリキャッシュ完了 ---");
		};
	}
}