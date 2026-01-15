package com.example.CashFlowWeb;

import java.util.List;

/**
 * 予測APIの結果を保持するモデルクラス。
 * 行動示唆フィードバックに対応するデータを追加済み。
 */
public class PredictionResult {
    // 既存フィールド
    private double averageMonthlyProfit;
    private int estimatedMonths;
    private String feedback;
    private double initialBalance;
    private List<Double> projectionPoints;
    
    // ▼▼▼ 新規追加フィールド (これがないとエラーになります) ▼▼▼
    private double shortfallAmount;   // 目標までの不足額（月あたり）
    private int delayMonths;          // 遅延月数
    private boolean isAchievable;     // 期限内に達成可能か

    // ▼▼▼ 8つの引数を受け取るコンストラクタ (これがないとエラーになります) ▼▼▼
    public PredictionResult(double averageMonthlyProfit, int estimatedMonths, String feedback, 
                            double initialBalance, List<Double> projectionPoints,
                            double shortfallAmount, int delayMonths, boolean isAchievable) {
        this.averageMonthlyProfit = averageMonthlyProfit;
        this.estimatedMonths = estimatedMonths;
        this.feedback = feedback;
        this.initialBalance = initialBalance;
        this.projectionPoints = projectionPoints;
        this.shortfallAmount = shortfallAmount;
        this.delayMonths = delayMonths;
        this.isAchievable = isAchievable;
    }

    public PredictionResult() {}

    // Getters
    public double getAverageMonthlyProfit() { return averageMonthlyProfit; }
    public int getEstimatedMonths() { return estimatedMonths; }
    public String getFeedback() { return feedback; }
    public double getInitialBalance() { return initialBalance; }
    public List<Double> getProjectionPoints() { return projectionPoints; }
    
    // 新規Getter
    public double getShortfallAmount() { return shortfallAmount; }
    public int getDelayMonths() { return delayMonths; }
    public boolean getIsAchievable() { return isAchievable; }
}
