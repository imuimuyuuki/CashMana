package com.example.CashFlowWeb;

import java.text.DecimalFormat;

/**
 * ポートフォリオの資産情報を保持するモデルクラス。
 */
public class Asset {
    private int id;
    private String name;
    private String tickerSymbol;
    private double quantity;
    private double purchasePrice;
    private double currentPrice;
    private String assetType;

    // このFORMATTERが使われていなかった
    private static final DecimalFormat FORMATTER = new DecimalFormat("#,###.##");

    public Asset() {}

    public Asset(int id, String name, String tickerSymbol, double quantity, double purchasePrice, double currentPrice, String assetType) {
        this.id = id;
        this.name = name;
        this.tickerSymbol = tickerSymbol;
        this.quantity = quantity;
        this.purchasePrice = purchasePrice;
        this.currentPrice = currentPrice;
        this.assetType = assetType;
    }

    // --- Getter Methods ---
    public int getId() { return id; }
    public String getName() { return name; }
    public String getTickerSymbol() { return tickerSymbol; }
    public double getQuantity() { return quantity; }
    public double getPurchasePrice() { return purchasePrice; }
    public double getCurrentPrice() { return currentPrice; }
    public String getAssetType() { return assetType; }

    // --- 計算プロパティ ---
    public double getPurchaseValue() {
        return quantity * purchasePrice;
    }

    public double getCurrentValue() {
        return quantity * currentPrice;
    }

    public double getGainLoss() {
        return getCurrentValue() - getPurchaseValue();
    }
    
    public double getGainLossRate() {
        if (getPurchaseValue() == 0) return 0.0;
        return (getGainLoss() / getPurchaseValue()) * 100;
    }
    
    // --- 【ここから追加】表示用フォーマットメソッド ---
    
    public String getFormattedPurchaseValue() {
        return "¥" + FORMATTER.format(getPurchaseValue());
    }

    public String getFormattedCurrentValue() {
        return "¥" + FORMATTER.format(getCurrentValue());
    }
    
    /**
     * 損益額を符号付きでフォーマットします。(例: +¥50,000)
     */
    public String getFormattedGainLoss() {
        double gainLoss = getGainLoss();
        String sign = gainLoss >= 0 ? "+" : "";
        return sign + "¥" + FORMATTER.format(gainLoss);
    }
}
