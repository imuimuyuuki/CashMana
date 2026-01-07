package com.example.CashFlowWeb;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final AssetDAO assetDAO = new AssetDAO();
    private final UserDAO userDAO = new UserDAO();

    public TransactionController() {
        DBManager.initializeDatabase();
        // categoryDAO.initializeCache(); // キャッシュ廃止に伴いコメントアウトまたは削除でも可
    }

    /**
     * ▼▼▼ 棒グラフ用：過去6ヶ月分の月次収支を集計して返すAPI ▼▼▼
     * URL: /api/transactions/monthly-summary
     */
    @GetMapping("/monthly-summary")
    public List<MonthlySummary> getMonthlySummary(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userDAO.findByUsername(userDetails.getUsername());

        // ユーザーの全取引を取得
        List<Transaction> transactions = transactionDAO.getAllTransactions(user.getId());

        // 月ごとに集計するためのマップ (キー: "2023-10" など)
        // TreeMapを使うことで日付順にソートされます
        Map<String, MonthlySummary> summaryMap = new TreeMap<>();

        // 直近6ヶ月分のキーを作っておく（データが0の月もグラフに表示するため）
        LocalDate today = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            String monthKey = today.minusMonths(i).format(DateTimeFormatter.ofPattern("yyyy-MM"));
            // 初期値0で埋める
            summaryMap.put(monthKey, new MonthlySummary(monthKey, 0, 0));
        }

        // 実際の取引データを集計
        for (Transaction t : transactions) {
            String monthKey = t.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM"));

            // 直近6ヶ月以内のデータなら加算する
            if (summaryMap.containsKey(monthKey)) {
                MonthlySummary current = summaryMap.get(monthKey);
                double newIncome = current.getTotalIncome();
                double newExpense = current.getTotalExpense();

                if ("INCOME".equals(t.getType())) {
                    newIncome += t.getAmount();
                } else {
                    newExpense += t.getAmount();
                }

                // 集計結果を更新
                summaryMap.put(monthKey, new MonthlySummary(monthKey, newIncome, newExpense));
            }
        }

        return new ArrayList<>(summaryMap.values());
    }

    /**
     * ▼▼▼ AI資産予測機能 ▼▼▼
     */
    @GetMapping("/predict")
    public ResponseEntity<PredictionResult> predictAssetGrowth(
            @RequestParam(defaultValue = "12") int monthsToPredict,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userDAO.findByUsername(userDetails.getUsername());
        int userId = user.getId();

        List<Transaction> allTransactions = transactionDAO.getAllTransactions(userId);

        // 1. 現状資産 (現金 + ポートフォリオ評価額)
        double currentCash = allTransactions.stream()
                .mapToDouble(t -> t.getType().equals("INCOME") ? t.getAmount() : -t.getAmount())
                .sum();

        double portfolioValue = assetDAO.getAllAssets(userId).stream()
                .mapToDouble(Asset::getCurrentValue)
                .sum();

        double initialBalance = currentCash + portfolioValue;

        // 2. 過去の月平均損益 (臨時収入を除く)
        Map<String, Double> monthlyProfits = allTransactions.stream()
                .filter(t -> !t.getIsExtraordinary())
                .collect(Collectors.groupingBy(
                        t -> t.getDate().toString().substring(0, 7),
                        Collectors.summingDouble(t -> t.getType().equals("INCOME") ? t.getAmount() : -t.getAmount())));

        double averageMonthlyProfit = monthlyProfits.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        // 3. 臨時支出のリスクシミュレーション
        List<Double> extraordinaryExpenses = allTransactions.stream()
                .filter(Transaction::getIsExtraordinary)
                .filter(t -> t.getType().equals("EXPENSE"))
                .map(Transaction::getAmount)
                .collect(Collectors.toList());

        Random random = new Random();
        List<Double> projectionPoints = new ArrayList<>();
        double simulatedBalance = initialBalance;
        projectionPoints.add(simulatedBalance);

        for (int i = 0; i < monthsToPredict; i++) {
            simulatedBalance += averageMonthlyProfit;
            if (!extraordinaryExpenses.isEmpty() && random.nextDouble() < 0.05) {
                double suddenExpense = extraordinaryExpenses.get(random.nextInt(extraordinaryExpenses.size()));
                simulatedBalance -= suddenExpense;
            }
            projectionPoints.add(simulatedBalance);
        }

        String feedback;
        if (averageMonthlyProfit > 0) {
            feedback = "順調です！このペースなら1年後には資産が約" + String.format("%,.0f", averageMonthlyProfit * 12) + "円増加する見込みです。";
        } else {
            feedback = "注意が必要です。現在の収支バランスでは資産が減少傾向にあります。固定費の見直しをお勧めします。";
        }

        return ResponseEntity.ok(new PredictionResult(averageMonthlyProfit, monthsToPredict, feedback, initialBalance,
                projectionPoints));
    }

    /**
     * 取引一覧取得 (検索フィルタ付き)
     */
    @GetMapping
    public List<Transaction> getTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String type,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userDAO.findByUsername(userDetails.getUsername());
        return transactionDAO.getFilteredTransactions(user.getId(), startDate, endDate, categoryId, type);
    }

    /**
     * ▼▼▼ 円グラフ用：カテゴリ別集計 ▼▼▼
     * URL: /api/transactions/summary
     */
    @GetMapping("/summary")
    public List<CategorySummary> getCategorySummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam String type,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userDAO.findByUsername(userDetails.getUsername());

        // フィルタリングされた取引を取得
        List<Transaction> txs = transactionDAO.getFilteredTransactions(user.getId(), startDate, endDate, null, type);

        // カテゴリごとに集計
        Map<String, Double> map = new HashMap<>();
        for (Transaction t : txs)
            map.put(t.getCategoryName(), map.getOrDefault(t.getCategoryName(), 0.0) + t.getAmount());

        // リストに変換して金額の多い順にソート
        List<CategorySummary> summaries = new ArrayList<>();
        map.forEach((name, amount) -> summaries.add(new CategorySummary(name, amount)));
        summaries.sort((a, b) -> Double.compare(b.getTotalAmount(), a.getTotalAmount()));

        return summaries;
    }

    @PostMapping
    public ResponseEntity<Void> addTransaction(@RequestBody Transaction transaction,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userDAO.findByUsername(userDetails.getUsername());
        boolean success = transactionDAO.addTransaction(
                user.getId(),
                transaction.getDate(), transaction.getAmount(), transaction.getType(),
                transaction.getCategoryId(), transaction.getIsFuture(), transaction.getIsExtraordinary());
        return success ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable int id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userDAO.findByUsername(userDetails.getUsername());
        Transaction transaction = transactionDAO.getTransactionById(id, user.getId());
        return (transaction != null) ? ResponseEntity.ok(transaction) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable int id) {
        // ※本来はここでもユーザーチェックが必要ですが、一旦そのまま
        return ResponseEntity.ok().build();
    }
}
