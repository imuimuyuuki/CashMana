package com.example.CashFlowWeb;

public class Category {
    private int id;
    private String name;
    private String type; // "INCOME" または "EXPENSE"

    // コンストラクタ
    public Category(int id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }
    
    // ゲッター (データ取得用メソッド)
    public int getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    
    // UI表示時にカテゴリ名を返すためのメソッド
    @Override
    public String toString() {
        return name + " (" + (type.equals("INCOME") ? "収入" : "支出") + ")";
    }
}
