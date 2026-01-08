package com.example.CashFlowWeb;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/fx")
public class FXController {package com.example.CashFlowWeb;

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
