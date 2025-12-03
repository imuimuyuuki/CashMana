package com.example.CashFlowWeb;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final AssetDAO assetDAO = new AssetDAO();

    public TransactionController() {
        DBManager.initializeDatabase();
        categoryDAO.initializeCache();
    }

    /**
     * ▼▼▼ 高度なAI資産予測機能 (Reality Simulation) ▼▼▼
     * 固定費(isExtraordinary=false)をベースラインとし、
     * 臨時支出(isExtraordinary=true)の発生確率と規模を確率的にシミュレーションします。
     */
    @GetMapping("/predict")
    public ResponseEntity<PredictionResult> predictAssetGrowth(
            @RequestParam(defaultValue = "12") int monthsToPredict
    ) {
        List<Transaction> allTransactions = transactionDAO.getAllTransactions();
        
        // 1. 現状資産 (現金 + ポートフォリオ評価額)
        double currentCash = allTransactions.stream()
                .mapToDouble(t -> t.getType().equals("INCOME") ? t.getAmount() : -t.getAmount())
                .sum();
        
        double currentPortfolio = assetDAO.getAllAssets().stream()
                                    .mapToDouble(Asset::getCurrentValue).sum();
        
        double totalAssets = currentCash + currentPortfolio;

        // 2. 直近6ヶ月のデータ分析
        LocalDate cutoff = LocalDate.now().minusMonths(6).withDayOfMonth(1);
        List<Transaction> recentTx = allTransactions.stream()
            .filter(t -> !t.getDate().isBefore(cutoff))
            .collect(Collectors.toList());

        if (recentTx.isEmpty()) {
             return ResponseEntity.ok(new PredictionResult(0, monthsToPredict, "データ不足のため分析できません。取引を記録してください。", totalAssets, new ArrayList<>(Collections.nCopies(monthsToPredict + 1, totalAssets))));
        }

        // A. 【固定収支】の計算 (臨時フラグがないものだけで、基礎体力を測る)
        double regularMonthlyNet = calculateRegularMonthlyNet(recentTx);

        // B. 【臨時支出】のリスク分析 (ユーザーの「癖」を数値化)
        List<Double> extraordinaryExpenses = recentTx.stream()
            .filter(t -> t.getIsExtraordinary() && t.getType().equals("EXPENSE"))
            .map(Transaction::getAmount)
            .collect(Collectors.toList());
            
        // 臨時支出の発生確率 (例: 6ヶ月で2回あったら 33%)
        double eventProbability = (double) extraordinaryExpenses.size() / 6.0;
        // 臨時支出の平均額
        double eventAvgAmount = extraordinaryExpenses.stream().mapToDouble(v->v).average().orElse(0.0);

        // 3. 未来シミュレーション (確率的アプローチ)
        List<Double> projectionPoints = new ArrayList<>();
        double simulatedAssets = totalAssets;
        projectionPoints.add(Math.floor(simulatedAssets)); // 現在

        // 投資リターン (ポートフォリオがあれば年利4%、なければ0.1%と仮定)
        double annualInterestRate = (currentPortfolio > 0) ? 0.04 : 0.001;
        double monthlyInvestRate = annualInterestRate / 12.0;
        
        Random random = new Random();

        for (int i = 1; i <= monthsToPredict; i++) {
            // STEP 1: 毎月の固定収支を加算 (ベースライン)
            simulatedAssets += regularMonthlyNet;

            // STEP 2: 確率で臨時支出が発生 (ユーザーの行動パターンを再現)
            // 乱数が発生確率を下回ったら、今月は「何か」が起きる
            if (eventAvgAmount > 0 && random.nextDouble() < eventProbability) {
                // 金額にも 0.8倍〜1.2倍 のゆらぎを持たせる（毎回同じ額ではない）
                double shock = eventAvgAmount * (0.8 + (random.nextDouble() * 0.4));
                simulatedAssets -= shock;
            }

            // STEP 3: 複利効果 (資産がプラスの場合のみ)
            // 資産額が増えるほど、ここで加算される額が増える＝曲線になる
            if (simulatedAssets > 0) {
                simulatedAssets *= (1 + monthlyInvestRate);
            }

            projectionPoints.add(Math.floor(simulatedAssets));
        }

        // 4. AIアドバイス生成
        String feedback = generateFeedback(regularMonthlyNet, eventProbability, monthsToPredict, projectionPoints, totalAssets);

        return ResponseEntity.ok(new PredictionResult(
            regularMonthlyNet, 
            monthsToPredict, 
            feedback, 
            totalAssets, 
            projectionPoints
        ));
    }

    // ヘルパー: 固定的な月次収支を計算
    private double calculateRegularMonthlyNet(List<Transaction> transactions) {
        Map<String, Double> monthlyNets = transactions.stream()
            .filter(t -> !t.getIsExtraordinary()) // ★重要: 臨時を除外して「実力値」を見る
            .collect(Collectors.groupingBy(
                t -> t.getDate().toString().substring(0, 7),
                Collectors.summingDouble(t -> t.getType().equals("INCOME") ? t.getAmount() : -t.getAmount())
            ));
            
        if (monthlyNets.isEmpty()) return 0.0;
        return monthlyNets.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    // ヘルパー: フィードバック生成
    private String generateFeedback(double regularNet, double eventProb, int months, List<Double> points, double initial) {
        double finalAmount = points.get(points.size() - 1);
        double growth = finalAmount - initial;

        if (regularNet < 0) {
            return "警告: 基礎生活費が赤字です。臨時支出を我慢しても資産が減る構造です。家賃やサブスク等の固定費を見直してください。";
        } else if (eventProb > 0.3) {
            return "分析: 基礎収支は黒字ですが、突発的な出費（趣味・旅行）が資産形成のブレーキになっています。イベント費用の予算化をお勧めします。";
        } else if (growth > initial * 0.1) {
            return "順調です！ 安定した生活費管理と複利効果により、資産は右肩上がりの理想的なカーブを描いています。";
        } else {
            return "安定的ですが、横ばいです。今の生活を維持できますが、資産を大きく増やすには「投資額を増やす」か「収入アップ」が必要です。";
        }
    }

    // ----------------------------------------
    // 既存API (変更なし)
    // ----------------------------------------

    @GetMapping
    public List<Transaction> getAllTransactions() { return transactionDAO.getAllTransactions(); }
    
    @GetMapping("/filter")
    public List<Transaction> getFilteredTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String type) {
        return transactionDAO.getFilteredTransactions(startDate, endDate, categoryId, type);
    }

    @GetMapping("/balance")
    public double getCurrentBalance() {
        return transactionDAO.getAllTransactions().stream()
                .mapToDouble(t -> t.getType().equals("INCOME") ? t.getAmount() : -t.getAmount()).sum();
    }
    
    @GetMapping("/summary")
    public List<MonthlySummary> getMonthlySummary() {
        List<Transaction> all = transactionDAO.getAllTransactions();
        Map<String, MonthlySummary> summaryMap = new TreeMap<>(Collections.reverseOrder());
        for (Transaction t : all) {
            String month = t.getDate().toString().substring(0, 7);
            MonthlySummary ms = summaryMap.getOrDefault(month, new MonthlySummary(month, 0, 0));
            if (t.getType().equals("INCOME")) ms = new MonthlySummary(month, ms.getTotalIncome() + t.getAmount(), ms.getTotalExpense());
            else ms = new MonthlySummary(month, ms.getTotalIncome(), ms.getTotalExpense() + t.getAmount());
            summaryMap.put(month, ms);
        }
        return new ArrayList<>(summaryMap.values());
    }
    
    @GetMapping("/summary/category")
    public List<CategorySummary> getCategorySummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam String type) {
        List<Transaction> txs = transactionDAO.getFilteredTransactions(startDate, endDate, null, type);
        Map<String, Double> map = new HashMap<>();
        for(Transaction t : txs) map.put(t.getCategoryName(), map.getOrDefault(t.getCategoryName(), 0.0) + t.getAmount());
        List<CategorySummary> summaries = new ArrayList<>();
        map.forEach((name, amount) -> summaries.add(new CategorySummary(name, amount)));
        summaries.sort((a, b) -> Double.compare(b.getTotalAmount(), a.getTotalAmount()));
        return summaries;
    }

    @PostMapping
    public ResponseEntity<Void> addTransaction(@RequestBody Transaction transaction) {
        boolean success = transactionDAO.addTransaction(
            transaction.getDate(), transaction.getAmount(), transaction.getType(),
            transaction.getCategoryId(), transaction.getIsFuture(), transaction.getIsExtraordinary()
        );
        return success ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable int id) {
        Transaction transaction = transactionDAO.getTransactionById(id);
        return (transaction != null) ? ResponseEntity.ok(transaction) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable int id) {
         return ResponseEntity.ok().build();
    }
}