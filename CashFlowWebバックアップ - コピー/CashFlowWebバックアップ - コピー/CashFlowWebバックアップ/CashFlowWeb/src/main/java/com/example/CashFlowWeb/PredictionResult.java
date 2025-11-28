package com.example.CashFlowWeb;

import java.util.List;

/**
 * 予測APIの結果を保持するモデルクラス。
 */
public class PredictionResult {
    private double averageMonthlyProfit;
    private int estimatedMonths;
    private String feedback;
    private double initialBalance;
    private List<Double> projectionPoints; // 予測グラフ用のデータポイント

    public PredictionResult(double averageMonthlyProfit, int estimatedMonths, String feedback, double initialBalance, List<Double> projectionPoints) {
        this.averageMonthlyProfit = averageMonthlyProfit;
        this.estimatedMonths = estimatedMonths;
        this.feedback = feedback;
        this.initialBalance = initialBalance;
        this.projectionPoints = projectionPoints;
    }
    
    public PredictionResult(double averageMonthlyProfit, int estimatedMonths, String feedback, double initialBalance) {
        this(averageMonthlyProfit, estimatedMonths, feedback, initialBalance, null);
    }

    // デフォルトコンストラクタ (JSONデシリアライズに必須)
    public PredictionResult() {}

    // Getter Methods
    public double getAverageMonthlyProfit() { return averageMonthlyProfit; }
    public int getEstimatedMonths() { return estimatedMonths; }
    public String getFeedback() { return feedback; }
    public double getInitialBalance() { return initialBalance; }
    public List<Double> getProjectionPoints() { return projectionPoints; }
}
