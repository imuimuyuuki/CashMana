package com.example.CashFlowWeb;

public class Goal {
    private int id;
    private String name;
    private double targetAmount;
    private double currentAmount;
    private String targetDate;
    private String imageUrl;

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public double getTargetAmount() { return targetAmount; }
    public double getCurrentAmount() { return currentAmount; }
    public String getTargetDate() { return targetDate; }
    public String getImageUrl() { return imageUrl; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setTargetAmount(double targetAmount) { this.targetAmount = targetAmount; }
    public void setCurrentAmount(double currentAmount) { this.currentAmount = currentAmount; }
    public void setTargetDate(String targetDate) { this.targetDate = targetDate; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
