package com.example.CashFlowWeb;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetDAO budgetDAO = new BudgetDAO();

    /**
     * 指定された月の予算状況を取得します。
     */
    @GetMapping
    public List<Budget> getBudgets(@RequestParam String yearMonth) {
        return budgetDAO.getBudgetsForMonth(yearMonth);
    }

    /**
     * 新しい予算を設定（または更新）します。
     */
    @PostMapping
    public ResponseEntity<Boolean> setBudget(@RequestBody Budget budget) {
        boolean success = budgetDAO.saveOrUpdateBudget(budget.getYearMonth(), budget.getCategoryId(), budget.getBudgetAmount());
        if (success) {
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.badRequest().build();
    }
}