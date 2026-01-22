package com.example.CashFlowWeb;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final AssetDAO assetDAO = new AssetDAO();
    private final UserDAO userDAO = new UserDAO();
    private final GoalDAO goalDAO = new GoalDAO();

    public TransactionController() {
        // DB初期化
        DBManager.initializeDatabase();
    }

    /**
     * 月次収支の集計データを取得します。
     */
    @GetMapping("/monthly-summary")
    public List<MonthlySummary> getMonthlySummary(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userDAO.findByUsername(userDetails.getUsername());
        List<Transaction> transactions = transactionDAO.getAllTransactions(user.getId());

        Map<String, double[]> aggMap = new TreeMap<>(); 

        for (Transaction t : transactions) {
            String month = t.getDate().toString().substring(0, 7);
            aggMap.putIfAbsent(month, new double[]{0.0, 0.0});

            if ("INCOME".equals(t.getType())) {
                aggMap.get(month)[0] += t.getAmount();
            } else if ("EXPENSE".equals(t.getType())) {
                aggMap.get(month)[1] += t.getAmount();
            }
        }

        List<MonthlySummary> summaryList = new ArrayList<>();
        for (Map.Entry<String, double[]> entry : aggMap.entrySet()) {
            summaryList.add(new MonthlySummary(entry.getKey(), entry.getValue()[0], entry.getValue()[1]));
        }
        return summaryList;
    }

    /**
     * AI資産予測シミュレーションと目標達成診断を行います。
     * ★目標未達時に具体的な遅延月数を計算し、警告を出します。
     */
    @GetMapping("/predict")
    public PredictionResult predictAssetGrowth(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userDAO.findByUsername(userDetails.getUsername());
        int userId = user.getId();

        // 1. 平均月次損益の計算
        List<Transaction> transactions = transactionDAO.getAllTransactions(userId);
        double averageMonthlyProfit = 0;

        if (!transactions.isEmpty()) {
            double totalIncome = transactions.stream()
                    .filter(t -> "INCOME".equals(t.getType()))
                    .mapToDouble(Transaction::getAmount).sum();
            double totalExpense = transactions.stream()
                    .filter(t -> "EXPENSE".equals(t.getType()))
                    .mapToDouble(Transaction::getAmount).sum();
            double totalProfit = totalIncome - totalExpense;

            long months = 1;
            if (transactions.size() > 1) {
                LocalDate oldest = transactions.stream().map(Transaction::getDate).min(LocalDate::compareTo).orElse(LocalDate.now());
                LocalDate newest = transactions.stream().map(Transaction::getDate).max(LocalDate::compareTo).orElse(LocalDate.now());
                
                months = ChronoUnit.MONTHS.between(oldest, newest);
                if (months < 1) months = 1;
            }
            averageMonthlyProfit = totalProfit / months;
        }

        // 2. 現在の資産総額を取得
        List<Asset> assets = assetDAO.getAllAssets(userId);
        double currentTotalAssets = assets.stream().mapToDouble(Asset::getCurrentValue).sum();

        // 3. 将来予測データの作成（6ヶ月分）
        List<Double> projectionPoints = new ArrayList<>();
        double projectedAmount = currentTotalAssets;
        for (int i = 0; i < 6; i++) {
            projectedAmount += averageMonthlyProfit;
            projectionPoints.add(projectedAmount);
        }

        // 4. 目標との比較・診断レポート生成
        List<Goal> goals = goalDAO.getAllGoals(userId);

        String feedback;
        boolean isAchievable = false;
        double shortfallAmount = 0;
        int delayMonths = 0;

        if (goals.isEmpty()) {
            feedback = "目標が設定されていません。目標管理ページでゴールを設定すると、AIが達成可能性を診断します。";
        } else {
            Goal targetGoal = goals.get(0);
            
            double targetAmount = targetGoal.getTargetAmount();
            LocalDate targetDate = LocalDate.parse(targetGoal.getTargetDate());
            LocalDate today = LocalDate.now();

            long monthsUntilTarget = ChronoUnit.MONTHS.between(today, targetDate);
            if (monthsUntilTarget <= 0) monthsUntilTarget = 1;

            double futureValueAtTarget = currentTotalAssets + (averageMonthlyProfit * monthsUntilTarget);

            if (futureValueAtTarget >= targetAmount) {
                // 達成可能
                isAchievable = true;
                feedback = "順調です。このペースなら目標「" + targetGoal.getName() + "」を期日までに達成できる見込みです。";
            } else {
                // 達成困難（赤警告用ロジック）
                isAchievable = false;
                shortfallAmount = targetAmount - futureValueAtTarget;

                // 遅延月数の計算
                if (averageMonthlyProfit > 0) {
                    double totalMonthsNeeded = (targetAmount - currentTotalAssets) / averageMonthlyProfit;
                    delayMonths = (int) Math.ceil(totalMonthsNeeded - monthsUntilTarget);
                } else {
                    delayMonths = 999; // 利益が出ていないため達成不能
                }
                
                String moneyFormat = String.format("%,.0f", shortfallAmount);
                String delayMsg;
                if (delayMonths > 900) {
                     delayMsg = "現在の収支状況では達成困難です。";
                } else {
                     delayMsg = "約" + delayMonths + "ヶ月の遅れが出る見込みです。";
                }

                feedback = "目標「" + targetGoal.getName() + "」に対し、" + delayMsg + 
                           "（不足額: " + moneyFormat + "円）" +
                           " 収支を見直してください。";
            }
        }

        return new PredictionResult(
            averageMonthlyProfit,
            6, 
            feedback,
            currentTotalAssets,
            projectionPoints,
            shortfallAmount,
            delayMonths,
            isAchievable
        );
    }

    /**
     * すべての取引履歴を取得します。
     */
    @GetMapping
    public List<Transaction> getTransactions(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userDAO.findByUsername(userDetails.getUsername());
        return transactionDAO.getAllTransactions(user.getId());
    }

    /**
     * カテゴリ別の支出集計を取得します（円グラフ用）。
     */
    @GetMapping("/category-summary")
    public List<CategorySummary> getCategorySummary(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userDAO.findByUsername(userDetails.getUsername());
        List<Transaction> transactions = transactionDAO.getAllTransactions(user.getId());

        Map<String, Double> summaryMap = transactions.stream()
                .filter(t -> "EXPENSE".equals(t.getType()))
                .collect(Collectors.groupingBy(
                        Transaction::getCategoryName,
                        Collectors.summingDouble(Transaction::getAmount)
                ));

        List<CategorySummary> result = new ArrayList<>();
        for (Map.Entry<String, Double> entry : summaryMap.entrySet()) {
            result.add(new CategorySummary(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    /**
     * 新しい取引を追加します。
     */
    @PostMapping
    public ResponseEntity<Boolean> addTransaction(@RequestBody Transaction t, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userDAO.findByUsername(userDetails.getUsername());
        boolean success = transactionDAO.addTransaction(
                user.getId(),
                t.getDate(),
                t.getAmount(),
                t.getType(),
                t.getCategoryId(),
                t.getGoalId(),
                t.getIsFuture(),
                t.getIsExtraordinary()
        );
        return success ? ResponseEntity.ok(true) : ResponseEntity.badRequest().body(false);
    }

    /**
     * 取引を更新します。
     */
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateTransaction(@PathVariable int id, @RequestBody Transaction t, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userDAO.findByUsername(userDetails.getUsername());
        
        Transaction transactionToUpdate = new Transaction(
            id,
            t.getDate(),
            t.getAmount(),
            t.getType(),
            t.getCategoryId(),
            null, 
            t.getGoalId(),
            null, 
            t.getIsFuture(),
            t.getIsExtraordinary()
        );

        boolean success = transactionDAO.updateTransaction(transactionToUpdate, user.getId());
        return success ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    /**
     * 取引を削除します。
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable int id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userDAO.findByUsername(userDetails.getUsername());
        boolean success = transactionDAO.deleteTransaction(id, user.getId());
        return success ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }
}
