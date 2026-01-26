package com.example.CashFlowWeb;

import java.text.DecimalFormat;

/**
 * 月次集計のデータを保持するモデルクラス。
 */
public class MonthlySummary {
    private String month;
    private double totalIncome;
    private double totalExpense;

    private static final DecimalFormat FORMATTER = new DecimalFormat("#,###円");

    // コンストラクタ
    public MonthlySummary(String month, double totalIncome, double totalExpense) {
        this.month = month;
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
    }

    // デフォルトコンストラクタ (JSONデシリアライズに必須)
    public MonthlySummary() {
    }

    // ----------------------------------------
    // Getter Methods
    // ----------------------------------------
    public String getMonth() {
        return month;
    }

    public double getTotalIncome() {
        return totalIncome;
    }

    public double getTotalExpense() {
        return totalExpense;
    }

    public double getNetProfit() {
        return totalIncome - totalExpense;
    }

    // フォーマット済み文字列
    public String getFormattedIncome() {
        return FORMATTER.format(totalIncome);
    }

    public String getFormattedExpense() {
        return FORMATTER.format(totalExpense);
    }

    public String getFormattedNetProfit() {
        return FORMATTER.format(getNetProfit());
    }
}
