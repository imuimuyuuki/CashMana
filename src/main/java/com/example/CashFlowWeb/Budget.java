package com.example.CashFlowWeb;

public class Budget {
    private int id;
    private String yearMonth;
    private int categoryId;
    private String categoryName;
    private double budgetAmount; // 予算額
    private double actualAmount; // 実績額

    // --- Getters ---
    public int getId() { return id; }
    public String getYearMonth() { return yearMonth; }
    public int getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public double getBudgetAmount() { return budgetAmount; }
    public double getActualAmount() { return actualAmount; }
    
    // --- Setters ---
    public void setId(int id) { this.id = id; }
    public void setYearMonth(String yearMonth) { this.yearMonth = yearMonth; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public void setBudgetAmount(double budgetAmount) { this.budgetAmount = budgetAmount; }
    public void setActualAmount(double actualAmount) { this.actualAmount = actualAmount; }
}
