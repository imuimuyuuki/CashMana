package com.example.CashFlowWeb;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetDAO budgetDAO = new BudgetDAO();

    @GetMapping
    public List<Budget> getBudgets(@AuthenticationPrincipal User user, @RequestParam String yearMonth) {
        return budgetDAO.getBudgetsForMonth(user.getId(), yearMonth);
    }

    @PostMapping
    public ResponseEntity<Boolean> setBudget(@AuthenticationPrincipal User user, @RequestBody Budget budget) {
        boolean success = budgetDAO.saveOrUpdateBudget(user.getId(), budget.getYearMonth(), budget.getCategoryId(),
                budget.getBudgetAmount());
        return success ? ResponseEntity.ok(true) : ResponseEntity.badRequest().build();
    }
}