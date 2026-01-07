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
        // ★修正ポイント: categoryDAO.initializeCache(); は削除しました
        DBManager.initializeDatabase();
    }

    /**
     * ▼▼▼ 棒グラフ用：過去6ヶ月分の月次収支を集計して返すAPI ▼▼▼
     */
    @GetMapping("/monthly-summary")
    public List<MonthlySummary> getMonthlySummary(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userDAO.findByUsername(userDetails.getUsername());
        List<Transaction> transactions = transactionDAO.getAllTransactions(user.getId());
        
        Map<String, MonthlySummary> summaryMap = new TreeMap<>(); 
        LocalDate today = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            String monthKey = today.minusMonths(i).format(DateTimeFormatter.ofPattern("yyyy-MM"));
            summaryMap.put(monthKey, new MonthlySummary(monthKey, 0, 0));
        }

        for (Transaction t : transactions) {
            String monthKey = t.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            if (summaryMap.containsKey(monthKey)) {
                MonthlySummary current = summaryMap.get(monthKey);
                double newIncome = current.getTotalIncome();
                double newExpense = current.getTotalExpense();
                if ("INCOME".equals(t.getType())) {
                    newIncome += t.getAmount();
                } else {
                    newExpense += t.getAmount();
                }
                summaryMap.put(monthKey, new MonthlySummary(monthKey, newIncome, newExpense));
            }
        }
        return new ArrayList<>(summaryMap.values());
    }

    @GetMapping("/predict")
    public ResponseEntity<PredictionResult> predictAssetGrowth(
            @RequestParam(defaultValue = "12") int monthsToPredict,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = userDAO.findByUsername(userDetails.getUsername());
        int userId = user.getId();
        List<Transaction> allTransactions = transactionDAO.getAllTransactions(userId);

        double currentCash = allTransactions.stream()
                .mapToDouble(t -> t.getType().equals("INCOME") ? t.getAmount() : -t.getAmount())
                .sum();

        double portfolioValue = assetDAO.getAllAssets(userId).stream()
                .mapToDouble(Asset::getCurrentValue)
                .sum();

        double initialBalance = currentCash + portfolioValue;

        Map<String, Double> monthlyProfits = allTransactions.stream()
                .filter(t -> !t.getIsExtraordinary()) 
                .collect(Collectors.groupingBy(
                        t -> t.getDate().toString().substring(0, 7), 
                        Collectors.summingDouble(t -> t.getType().equals("INCOME") ? t.getAmount() : -t.getAmount())
                ));

        double averageMonthlyProfit = monthlyProfits.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

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
        return ResponseEntity.ok(new PredictionResult(averageMonthlyProfit, monthsToPredict, feedback, initialBalance, projectionPoints));
    }

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

    @GetMapping("/summary")
    public List<CategorySummary> getCategorySummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam String type,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userDAO.findByUsername(userDetails.getUsername());
        List<Transaction> txs = transactionDAO.getFilteredTransactions(user.getId(), startDate, endDate, null, type);
        
        Map<String, Double> map = new HashMap<>();
        for (Transaction t : txs)
            map.put(t.getCategoryName(), map.getOrDefault(t.getCategoryName(), 0.0) + t.getAmount());
        
        List<CategorySummary> summaries = new ArrayList<>();
        map.forEach((name, amount) -> summaries.add(new CategorySummary(name, amount)));
        summaries.sort((a, b) -> Double.compare(b.getTotalAmount(), a.getTotalAmount()));
        return summaries;
    }

    @PostMapping
    public ResponseEntity<Void> addTransaction(@RequestBody Transaction transaction, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userDAO.findByUsername(userDetails.getUsername());
        boolean success = transactionDAO.addTransaction(
                user.getId(),
                transaction.getDate(), transaction.getAmount(), transaction.getType(),
                transaction.getCategoryId(), transaction.getIsFuture(), transaction.getIsExtraordinary());
        return success ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable int id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userDAO.findByUsername(userDetails.getUsername());
        Transaction transaction = transactionDAO.getTransactionById(id, user.getId());
        return (transaction != null) ? ResponseEntity.ok(transaction) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable int id) {
        return ResponseEntity.ok().build();
    }
}
