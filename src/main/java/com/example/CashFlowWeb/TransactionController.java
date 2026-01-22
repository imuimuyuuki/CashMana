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
        DBManager.initializeDatabase();
    }

    @GetMapping("/monthly-summary")
    public List<MonthlySummary> getMonthlySummary(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userDAO.findByUsername(userDetails.getUsername());
        List<Transaction> transactions = transactionDAO.getAllTransactions(user.getId());
        Map<String, double[]> aggMap = new TreeMap<>(); 

        for (Transaction t : transactions) {
            String month = t.getDate().toString().substring(0, 7);
            aggMap.putIfAbsent(month, new double[]{0.0, 0.0});
            if ("INCOME".equals(t.getType())) aggMap.get(month)[0] += t.getAmount();
            else if ("EXPENSE".equals(t.getType())) aggMap.get(month)[1] += t.getAmount();
        }

        List<MonthlySummary> summaryList = new ArrayList<>();
        for (Map.Entry<String, double[]> entry : aggMap.entrySet()) {
            summaryList.add(new MonthlySummary(entry.getKey(), entry.getValue()[0], entry.getValue()[1]));
        }
        return summaryList;
    }

    // ▼▼▼ ここが今回の修正のメイン箇所です ▼▼▼
    @GetMapping("/predict")
    public PredictionResult predictAssetGrowth(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userDAO.findByUsername(userDetails.getUsername());
        int userId = user.getId();

        // 1. 平均月次損益
        List<Transaction> transactions = transactionDAO.getAllTransactions(userId);
        double averageMonthlyProfit = 0;
        if (!transactions.isEmpty()) {
            double totalIncome = transactions.stream().filter(t -> "INCOME".equals(t.getType())).mapToDouble(Transaction::getAmount).sum();
            double totalExpense = transactions.stream().filter(t -> "EXPENSE".equals(t.getType())).mapToDouble(Transaction::getAmount).sum();
            
            long months = 1;
            if (transactions.size() > 1) {
                LocalDate oldest = transactions.stream().map(Transaction::getDate).min(LocalDate::compareTo).orElse(LocalDate.now());
                LocalDate newest = transactions.stream().map(Transaction::getDate).max(LocalDate::compareTo).orElse(LocalDate.now());
                months = ChronoUnit.MONTHS.between(oldest, newest);
                if (months < 1) months = 1;
            }
            averageMonthlyProfit = (totalIncome - totalExpense) / months;
        }

        // 2. 現在資産
        List<Asset> assets = assetDAO.getAllAssets(userId);
        double currentTotalAssets = assets.stream().mapToDouble(Asset::getCurrentValue).sum();

        // 3. グラフ用データ
        List<Double> projectionPoints = new ArrayList<>();
        double projectedAmount = currentTotalAssets;
        for (int i = 0; i < 6; i++) {
            projectedAmount += averageMonthlyProfit;
            projectionPoints.add(projectedAmount);
        }

        // 4. 目標診断と遅延月数の計算
        List<Goal> goals = goalDAO.getAllGoals(userId);
        String feedback;
        boolean isAchievable = false;
        double shortfallAmount = 0;
        int delayMonths = 0;

        if (goals.isEmpty()) {
            feedback = "目標が設定されていません。目標を設定すると、AIが達成見込みを診断します。";
        } else {
            Goal targetGoal = goals.get(0);
            double targetAmount = targetGoal.getTargetAmount();
            LocalDate targetDate = LocalDate.parse(targetGoal.getTargetDate());
            long monthsUntilTarget = ChronoUnit.MONTHS.between(LocalDate.now(), targetDate);
            if (monthsUntilTarget <= 0) monthsUntilTarget = 1;

            double futureValue = currentTotalAssets + (averageMonthlyProfit * monthsUntilTarget);

            if (futureValue >= targetAmount) {
                isAchievable = true;
                feedback = "順調です！このペースなら目標「" + targetGoal.getName() + "」を期日までに達成できる見込みです。";
            } else {
                isAchievable = false;
                shortfallAmount = targetAmount - futureValue;
                
                // 遅れ月数を計算
                if (averageMonthlyProfit > 0) {
                    double totalMonthsNeeded = (targetAmount - currentTotalAssets) / averageMonthlyProfit;
                    delayMonths = (int) Math.ceil(totalMonthsNeeded - monthsUntilTarget);
                } else {
                    delayMonths = 999; // 達成不能
                }

                // ★ここに「何ヶ月遅れそう」というメッセージを入れました
                String delayMsg = (delayMonths > 900) ? "現在の収支では達成困難です。" : "約" + delayMonths + "ヶ月の遅れが出る見込みです。";
                feedback = "目標「" + targetGoal.getName() + "」の達成に黄色信号です。" + delayMsg + 
                           "（不足額: ¥" + String.format("%,.0f", shortfallAmount) + "）";
            }
        }

        return new PredictionResult(averageMonthlyProfit, 6, feedback, currentTotalAssets, projectionPoints, shortfallAmount, delayMonths, isAchievable);
    }

    // --- 以下、既存のメソッド（変更なし） ---
    @GetMapping
    public List<Transaction> getTransactions(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userDAO.findByUsername(userDetails.getUsername());
        return transactionDAO.getAllTransactions(user.getId());
    }

    @GetMapping("/category-summary")
    public List<CategorySummary> getCategorySummary(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userDAO.findByUsername(userDetails.getUsername());
        List<Transaction> transactions = transactionDAO.getAllTransactions(user.getId());
        Map<String, Double> summaryMap = transactions.stream()
                .filter(t -> "EXPENSE".equals(t.getType()))
                .collect(Collectors.groupingBy(Transaction::getCategoryName, Collectors.summingDouble(Transaction::getAmount)));
        List<CategorySummary> result = new ArrayList<>();
        summaryMap.forEach((k, v) -> result.add(new CategorySummary(k, v)));
        return result;
    }

    @PostMapping
    public ResponseEntity<Boolean> addTransaction(@RequestBody Transaction t, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userDAO.findByUsername(userDetails.getUsername());
        return transactionDAO.addTransaction(user.getId(), t.getDate(), t.getAmount(), t.getType(), t.getCategoryId(), t.getGoalId(), t.getIsFuture(), t.getIsExtraordinary()) 
               ? ResponseEntity.ok(true) : ResponseEntity.badRequest().body(false);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateTransaction(@PathVariable int id, @RequestBody Transaction t, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userDAO.findByUsername(userDetails.getUsername());
        Transaction transactionToUpdate = new Transaction(id, t.getDate(), t.getAmount(), t.getType(), t.getCategoryId(), null, t.getGoalId(), null, t.getIsFuture(), t.getIsExtraordinary());
        return transactionDAO.updateTransaction(transactionToUpdate, user.getId()) ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable int id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userDAO.findByUsername(userDetails.getUsername());
        return transactionDAO.deleteTransaction(id, user.getId()) ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }
}
