package com.example.CashFlowWeb;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/fx")
public class FXController {package com.example.Capackage com.example.CashFlowWeb;

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
}shFlowWeb;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate; // 外部API呼び出し用
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/fx")
public class FXController {

    // チャート履歴を保持するリスト
    private static final List<Double> priceHistory = new CopyOnWriteArrayList<>();
    private static double currentRate = 153.0; // 初期値（外部取得失敗時用）

    // 起動時に1回だけ実行される初期化ブロック
    static {
        // 1. 本物のレートを取得しに行く
        fetchRealRate();

        // 2. 過去30回分の履歴データを生成（グラフがいきなり描画されるように）
        double rate = currentRate;
        for (int i = 0; i < 30; i++) {
            // 履歴は「現在より過去」なので、逆算して少しずつズラして作る
            priceHistory.add(0, rate); 
            rate += (Math.random() - 0.5) * 0.5;
        }
    }

    // 外部APIから本物のレートを取得するメソッド
    private static void fetchRealRate() {
        try {
            String url = "https://api.exchangerate-api.com/v4/latest/USD";
            RestTemplate restTemplate = new RestTemplate();
            String result = restTemplate.getForObject(url, String.class);
            
            // JSONを解析して JPY の値を取り出す
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(result);
            double realRate = root.path("rates").path("JPY").asDouble();
            
            if (realRate > 0) {
                currentRate = realRate;
                System.out.println("現在のUSD/JPYレートを取得しました: " + currentRate);
            }
        } catch (Exception e) {
            System.err.println("レート取得に失敗しました。デフォルト値を使用します: " + e.getMessage());
            // 失敗時は153円などを維持
        }
    }

    // レスポンス用のデータクラス
    static class FXPrediction {
        public double currentRate;
        public String recommendation; // "BUY", "SELL", "STAY"
        public String reason;
        public List<Double> history; 
        public List<Double> prediction; 
        public double riskPercentage; 

        public FXPrediction(double currentRate, String recommendation, String reason, List<Double> history, List<Double> prediction, double riskPercentage) {
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
        // 1. リアルタイム変動シミュレーション
        // 実際のレートをベースに、小刻みに変動させる（API制限回避のため毎秒外部通信はしない）
        double volatility = 0.05; // 変動幅
        double change = (Math.random() - 0.5) * volatility;
        currentRate += change;

        // 履歴に追加
        priceHistory.add(currentRate);
        if (priceHistory.size() > 30) {
            priceHistory.remove(0); // 古いものを削除
        }

        // 2. AI予測ロジック
        // 直近5回の平均と現在値を比較
        double recentAvg = priceHistory.stream()
                .skip(Math.max(0, priceHistory.size() - 5))
                .mapToDouble(Double::doubleValue)
                .average().orElse(currentRate);

        String recommendation;
        String reason;
        List<Double> prediction = new ArrayList<>();
        double predRate = currentRate;

        // トレンド判定
        if (currentRate < recentAvg - 0.2) {
            recommendation = "BUY"; 
            reason = "短期的には売られすぎの傾向があります。AIモデルは今後1週間のリバウンド（円安方向への修正）を予測しています。";
            // 上昇トレンド予測を作成
            for(int i=0; i<7; i++) {
                predRate += 0.1 + (Math.random() * 0.2);
                prediction.add(predRate);
            }
        } else if (currentRate > recentAvg + 0.2) {
            recommendation = "SELL"; 
            reason = "過熱感があります。AIモデルは調整局面（円高）入りを示唆しており、利益確定を推奨します。";
            // 下降トレンド予測を作成
            for(int i=0; i<7; i++) {
                predRate -= 0.1 + (Math.random() * 0.2);
                prediction.add(predRate);
            }
        } else {
            recommendation = "STAY"; 
            reason = "方向感が乏しい展開です。重要な経済指標の発表を待ち、トレンドが明確になるまで静観を推奨します。";
            // 横ばい予測を作成
            for(int i=0; i<7; i++) {
                predRate += (Math.random() - 0.5) * 0.2;
                prediction.add(predRate);
            }
        }

        double risk = Math.random() * 3 + 1; // 1~4%のリスク

        return new FXPrediction(currentRate, recommendation, reason, new ArrayList<>(priceHistory), prediction, risk);
    }
}

    // 簡易的なAI予測レスポンスモデル
    static class FXPrediction {
        public double currentRate;
        public String recommendation; // "BUY", "SELL", "STAY"
        public String reason;
        public List<Double> history; // 過去のチャートデータ
        public List<Double> prediction; // 未来の予測データ
        public double riskPercentage; // リスク予測

        public FXPrediction(double currentRate, String recommendation, String reason, List<Double> history, List<Double> prediction, double riskPercentage) {
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
        // 本来は外部APIから取得・機械学習モデルで計算しますが、今回はシミュレーション用に生成します。
        
        double baseRate = 145.0 + (Math.random() * 5 - 2.5); // 142.5 ~ 147.5 の間で変動
        
        // 1. 過去データの生成 (直近30日)
        List<Double> history = new ArrayList<>();
        double rate = baseRate;
        for (int i = 0; i < 30; i++) {
            history.add(rate);
            rate += (Math.random() - 0.5) * 1.5; // ランダムウォーク
        }
        double currentRate = history.get(history.size() - 1);

        // 2. AI予測ロジック (簡易シミュレーション)
        // トレンド判定: 過去5日間の平均と比較
        double recentAvg = history.subList(25, 30).stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        String recommendation;
        String reason;
        List<Double> prediction = new ArrayList<>();
        
        double predRate = currentRate;

        if (currentRate < recentAvg - 1.0) {
            recommendation = "BUY"; // 買い時 (安値圏)
            reason = "短期的には売られすぎの傾向があります。AIモデルは今後1週間のリバウンド（円安方向への修正）を予測しています。";
            // 上昇トレンドを予測
            for(int i=0; i<7; i++) {
                predRate += 0.2 + (Math.random() * 0.3);
                prediction.add(predRate);
            }
        } else if (currentRate > recentAvg + 1.0) {
            recommendation = "SELL"; // 売り時 (高値圏)
            reason = "過熱感があります。AIモデルは調整局面（円高）入りを示唆しており、利益確定を推奨します。";
            // 下降トレンドを予測
            for(int i=0; i<7; i++) {
                predRate -= 0.2 + (Math.random() * 0.3);
                prediction.add(predRate);
            }
        } else {
            recommendation = "STAY"; // ステイ
            reason = "方向感が乏しい展開です。重要な経済指標の発表を待ち、トレンドが明確になるまで静観を推奨します。";
            // 横ばいを予測
            for(int i=0; i<7; i++) {
                predRate += (Math.random() - 0.5);
                prediction.add(predRate);
            }
        }

        // 3. リスク計算
        double risk = Math.random() * 10 + 5; // 5% ~ 15% のリスク

        return new FXPrediction(currentRate, recommendation, reason, history, prediction, risk);
    }
}
