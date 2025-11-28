package com.example.CashFlowWeb;

import java.text.DecimalFormat;
import java.time.LocalDate;

/**
 * 取引データを保持するモデルクラス。
 */
public class Transaction {
    private int id;
    private LocalDate date;
    private double amount;
    private String type; // INCOME (収入) or EXPENSE (支出)
    private int categoryId;
    private String categoryName;
    private boolean isFuture; // 未来の予定取引フラグ
    private boolean isExtraordinary; // 臨時収支フラグ (定常的なら false, 臨時的なら true)

    private static final DecimalFormat FORMATTER = new DecimalFormat("#,###");

    // ----------------------------------------
    // コンストラクタ
    // ----------------------------------------

    // 全フィールドを持つコンストラクタ (DBからの取得用)
    public Transaction(int id, LocalDate date, double amount, String type, int categoryId, String categoryName, boolean isFuture, boolean isExtraordinary) {
        this.id = id;
        this.date = date;
        this.amount = amount;
        this.type = type;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.isFuture = isFuture;
        this.isExtraordinary = isExtraordinary;
    }
    
    // 取引登録時に使用するコンストラクタ（IDなし）
    public Transaction(LocalDate date, double amount, String type, int categoryId, boolean isFuture, boolean isExtraordinary) {
        this.date = date;
        this.amount = amount;
        this.type = type;
        this.categoryId = categoryId;
        this.isFuture = isFuture;
        this.isExtraordinary = isExtraordinary;
    }

    // デフォルトコンストラクタ (Spring BootのJSONデシリアライズに必須)
    public Transaction() {}

    // ----------------------------------------
    // Getter Methods (Spring BootがJSON変換に利用)
    // ----------------------------------------
    
    public int getId() { return id; }
    public LocalDate getDate() { return date; }
    public double getAmount() { return amount; }
    public String getType() { return type; }
    public int getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public boolean getIsFuture() { return isFuture; }
    public boolean getIsExtraordinary() { return isExtraordinary; }

    // 補助Getter
    public String getFormattedAmount() {
        return FORMATTER.format(amount) + "円";
    }
}