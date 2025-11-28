package com.example.CashFlowWeb;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionDAO transactionDAO = new TransactionDAO();

    // 1. 全取引取得
    @GetMapping
    public List<Transaction> getAllTransactions(@AuthenticationPrincipal User user) {
        return transactionDAO.getAllTransactions(user.getId());
    }

    // 2. フィルタリング検索
    @GetMapping("/filter")
    public List<Transaction> getFilteredTransactions(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String type) {
        return transactionDAO.getFilteredTransactions(user.getId(), startDate, endDate, categoryId, type);
    }

    // 3. 【追加】現在の資産残高を取得 (/balance)
    @GetMapping("/balance")
    public ResponseEntity<Double> getCurrentBalance(@AuthenticationPrincipal User user) {
        double balance = transactionDAO.calculateCurrentBalance(user.getId());
        return ResponseEntity.ok(balance);
    }

    // 4. 【修正】カテゴリ別集計 (円グラフ用) - URLを /summary/category に変更
    @GetMapping("/summary/category")
    public List<CategorySummary> getCategorySummary(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String type) {

        // 指定期間の取引を取得して集計
        List<Transaction> txs = transactionDAO.getFilteredTransactions(user.getId(), startDate, endDate, null, type);

        Map<String, Double> map = new HashMap<>();
        for (Transaction t : txs) {
            map.put(t.getCategoryName(), map.getOrDefault(t.getCategoryName(), 0.0) + t.getAmount());
        }

        List<CategorySummary> summaries = new ArrayList<>();
        map.forEach((name, amount) -> summaries.add(new CategorySummary(name, amount)));
        summaries.sort((a, b) -> Double.compare(b.getTotalAmount(), a.getTotalAmount()));
        return summaries;
    }

    // 5. 【追加】月別集計 (棒グラフ用) (/summary/monthly)
    @GetMapping("/summary/monthly")
    public List<MonthlySummary> getMonthlySummary(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return transactionDAO.getMonthlySummary(user.getId(), startDate, endDate);
    }

    // 6. 取引追加
    @PostMapping
    public ResponseEntity<Void> addTransaction(@AuthenticationPrincipal User user,
            @RequestBody Transaction transaction) {
        boolean success = transactionDAO.addTransaction(
                user.getId(),
                transaction.getDate(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getCategoryId(),
                transaction.getIsFuture(),
                transaction.getIsExtraordinary());
        return success ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    // 7. 単一取得
    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@AuthenticationPrincipal User user, @PathVariable int id) {
        Transaction transaction = transactionDAO.getTransactionById(id, user.getId());
        return (transaction != null) ? ResponseEntity.ok(transaction) : ResponseEntity.notFound().build();
    }

    // 8. 削除
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@AuthenticationPrincipal User user, @PathVariable int id) {
        boolean success = transactionDAO.deleteTransaction(id, user.getId());
        return success ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    // 9. 予測機能
    @GetMapping("/predict")
    public ResponseEntity<PredictionResult> predictGoalAchievement(@AuthenticationPrincipal User user) {
        LocalDate threeMonthsAgo = LocalDate.now().minusMonths(3);
        List<Transaction> recentTransactions = transactionDAO.getFilteredTransactions(user.getId(), threeMonthsAgo,
                LocalDate.now(), null, null);

        double totalIncome = 0;
        double totalExpense = 0;
        for (Transaction t : recentTransactions) {
            if (t.getIsExtraordinary())
                continue;
            if ("INCOME".equals(t.getType()))
                totalIncome += t.getAmount();
            else if ("EXPENSE".equals(t.getType()))
                totalExpense += t.getAmount();
        }
        double averageProfit = (totalIncome - totalExpense) / 3.0;

        int estimatedMonths = (averageProfit <= 0) ? -1 : (int) Math.ceil(100000 / averageProfit);
        String feedback = (estimatedMonths == -1) ? "目標達成困難" : estimatedMonths + "ヶ月で達成可能";

        // 現在の残高も渡す
        double currentBalance = transactionDAO.calculateCurrentBalance(user.getId());

        return ResponseEntity.ok(new PredictionResult(averageProfit, estimatedMonths, feedback, currentBalance));
    }
}