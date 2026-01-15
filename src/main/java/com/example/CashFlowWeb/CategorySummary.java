package com.example.CashFlowWeb;

/**
 * 円グラフ用のカテゴリ別集計データを保持するモデルクラス。
 */
public class CategorySummary {
    private String categoryName;
    private double totalAmount;

    // コンストラクタ
    public CategorySummary(String categoryName, double totalAmount) {
        this.categoryName = categoryName;
        this.totalAmount = totalAmount;
    }
    
    // デフォルトコンストラクタ (JSON変換用)
    public CategorySummary() {}

    // ゲッター (Spring BootがJSONに変換する際に必要)
    public String getCategoryName() {
        return categoryName;
    }

    public double getTotalAmount() {
        return totalAmount;
    }
}
