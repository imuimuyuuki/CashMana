package com.example.CashFlowWeb;

import java.text.DecimalFormat;
import java.time.LocalDate;

public class Transaction {
    private int id;
    private LocalDate date;
    private double amount;
    private String type;
    private int categoryId;
    private String categoryName;
    private Integer goalId; // ★追加: 紐付く目標ID (null可)
    private String goalName; // ★追加: 表示用
    private boolean isFuture;
    private boolean isExtraordinary;

    private static final DecimalFormat FORMATTER = new DecimalFormat("#,###");

    public Transaction() {}

    public Transaction(int id, LocalDate date, double amount, String type, int categoryId, String categoryName, Integer goalId, String goalName, boolean isFuture, boolean isExtraordinary) {
        this.id = id;
        this.date = date;
        this.amount = amount;
        this.type = type;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.goalId = goalId;
        this.goalName = goalName;
        this.isFuture = isFuture;
        this.isExtraordinary = isExtraordinary;
    }

    // Getter / Setter
    public int getId() { return id; }
    public LocalDate getDate() { return date; }
    public double getAmount() { return amount; }
    public String getType() { return type; }
    public int getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public Integer getGoalId() { return goalId; } // ★
    public String getGoalName() { return goalName; } // ★
    public boolean getIsFuture() { return isFuture; }
    public boolean getIsExtraordinary() { return isExtraordinary; }

    public void setGoalId(Integer goalId) { this.goalId = goalId; }
}
