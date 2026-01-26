package com.example.CashFlowWeb;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/fx")
public class FXController {

    // チャート履歴を保持するリスト
    private static final List<Double> priceHistory = new CopyOnWriteArrayList<>();
    private static double currentRate = 150.0; // 初期プレースホルダー（取得失敗時用）

    // Yahoo Finance API URL (USD/JPY)
    private static final String YAHOO_API_URL = "https://query1.finance.yahoo.com/v8/finance/chart/USDJPY=X?interval=1m&range=1d";

    // 起動時に初期データを取得
    static {
        // 初回の実データ取得
        double rate = fetchRealTimeRate();
        if (rate > 0) {
            currentRate = rate;
        }

        // グラフがいきなり空っぽにならないよう、現在のレートで初期埋めする
        // (アプリが動いている間に、ここがリアルな変動データに置き換わっていきます)
        for (int i = 0; i < 30; i++) {
            priceHistory.add(currentRate);
        }
    }

    // ★重要: Yahoo Financeからリアルタイムレートを取得するメソッド
    private static double fetchRealTimeRate() {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // ブラウザからのアクセスに見せかけるためのヘッダー（これがないと拒否されることがある）
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(YAHOO_API_URL, HttpMethod.GET, entity,
                    String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            // JSON構造: chart -> result -> [0] -> meta -> regularMarketPrice
            JsonNode meta = root.path("chart").path("result").get(0).path("meta");
            double price = meta.path("regularMarketPrice").asDouble();

            return price;
        } catch (Exception e) {
            System.err.println("リアルタイムレート取得エラー: " + e.getMessage());
            return -1; // 失敗
        }
    }

    static class FXPrediction {
        public double currentRate;
        public String recommendation;
        public String reason;
        public List<Double> history;
        public List<Double> prediction;
        public double riskPercentage;

        public FXPrediction(double currentRate, String recommendation, String reason, List<Double> history,
                List<Double> prediction, double riskPercentage) {
            this.currentRate = currentRate;
            this.recommendation = recommendation;
            this.reason = reason;
            this.history = history;
            this.prediction = prediction;
            this.riskPercentage = riskPercentage;
        }
    }

    @GetMapping("/analysis")
    public FXPrediction getFXAnalysis() {
        // 1. リアルタイムレートを取得
        double realRate = fetchRealTimeRate();

        // 取得成功したら更新、失敗したら前回の値を維持
        if (realRate > 0) {
            currentRate = realRate;
        }

        // 履歴に追加 (常に最新30件を保持)
        priceHistory.add(currentRate);
        if (priceHistory.size() > 30) {
            priceHistory.remove(0);
        }

        // 2. AIトレンド判定ロジック (実際の移動平均線との乖離を見る)
        double recentAvg = priceHistory.stream()
                .mapToDouble(Double::doubleValue)
                .average().orElse(currentRate);

        String recommendation;
        String reason;
        List<Double> prediction = new ArrayList<>();
        double predRate = currentRate;

        // トレンド判定の閾値（0.05円の乖離で反応）
        double threshold = 0.05;

        if (currentRate < recentAvg - threshold) {
            recommendation = "BUY";
            reason = "AI分析: 現在値(" + currentRate + ")は平均線(" + String.format("%.2f", recentAvg)
                    + ")を下回っています。反発（押し目買い）の好機です。";
            // 上昇トレンド予測を生成
            for (int i = 0; i < 7; i++) {
                predRate += 0.02 + (Math.random() * 0.05);
                prediction.add(predRate);
            }
        } else if (currentRate > recentAvg + threshold) {
            recommendation = "SELL";
            reason = "AI分析: 現在値(" + currentRate + ")は平均線(" + String.format("%.2f", recentAvg)
                    + ")を上回っています。高値警戒感があり、利益確定を推奨します。";
            // 下降トレンド予測を生成
            for (int i = 0; i < 7; i++) {
                predRate -= 0.02 + (Math.random() * 0.05);
                prediction.add(predRate);
            }
        } else {
            recommendation = "STAY";
            reason = "AI分析: 現在値は平均線付近で推移しており、方向感が定まっていません。明確なトレンド発生を待機してください。";
            // 横ばい予測を生成
            for (int i = 0; i < 7; i++) {
                predRate += (Math.random() - 0.5) * 0.05;
                prediction.add(predRate);
            }
        }

        // ボラティリティ（変動幅）からリスクを簡易計算
        double maxVal = Collections.max(priceHistory);
        double minVal = Collections.min(priceHistory);
        double risk = ((maxVal - minVal) / currentRate) * 100 * 10; // 見やすいように係数をかける

        return new FXPrediction(currentRate, recommendation, reason, new ArrayList<>(priceHistory), prediction, risk);
    }
}
