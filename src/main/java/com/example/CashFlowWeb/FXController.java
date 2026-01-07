package com.example.CashFlowWeb;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/fx")
public class FXController {

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
