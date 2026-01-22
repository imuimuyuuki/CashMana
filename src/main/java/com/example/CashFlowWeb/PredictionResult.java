package com.example.CashFlowWeb;

import java.util.List;

public class PredictionResult {
    private double averageMonthlyProfit;
    private int estimatedMonths;
    private String feedback;
    private double initialBalance;
    private List<Double> projectionPoints;

    // ▼ 追加したフィールド
    private double shortfallAmount;   // 不足額
    private int delayMonths;          // 遅れ月数
    private boolean isAchievable;     // 達成可能かどうか

    public PredictionResult() {}

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

    // Getters
    public double getAverageMonthlyProfit() { return averageMonthlyProfit; }
    public int getEstimatedMonths() { return estimatedMonths; }
    public String getFeedback() { return feedback; }
    public double getInitialBalance() { return initialBalance; }
    public List<Double> getProjectionPoints() { return projectionPoints; }
    
    public double getShortfallAmount() { return shortfallAmount; }
    public int getDelayMonths() { return delayMonths; }
    public boolean getIsAchievable() { return isAchievable; }
}
