package com.example.CashFlowWeb;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetDAO budgetDAO = new BudgetDAO();
    private final UserDAO userDAO = new UserDAO();

    /**
     * 指定された月の予算状況を取得します。
     */
    @GetMapping
    public List<Budget> getBudgets(@RequestParam String yearMonth, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userDAO.findByUsername(userDetails.getUsername());
        // ★ userId を渡す
        return budgetDAO.getBudgetsForMonth(yearMonth, user.getId());
    }

    /**
     * 新しい予算を設定（または更新）します。
     */
    @PostMapping
    public ResponseEntity<Boolean> setBudget(@RequestBody Budget budget,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userDAO.findByUsername(userDetails.getUsername());

        // ★ userId を渡す
        boolean success = budgetDAO.saveOrUpdateBudget(
                budget.getYearMonth(),
                budget.getCategoryId(),
                budget.getBudgetAmount(),
                user.getId());

        if (success) {
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.badRequest().build();
    }
}
