package com.example.CashFlowWeb;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalDAO goalDAO = new GoalDAO();

    @GetMapping
    public List<Goal> getAllGoals(@AuthenticationPrincipal User user) {
        return goalDAO.getAllGoals(user.getId());
    }

    @PostMapping
    public ResponseEntity<Void> addGoal(@AuthenticationPrincipal User user, @RequestBody Goal goal) {
        boolean success = goalDAO.addGoal(goal, user.getId());
        return success ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateGoal(@AuthenticationPrincipal User user, @PathVariable int id,
            @RequestBody Goal goal) {
        goal.setId(id);
        boolean success = goalDAO.updateGoal(goal, user.getId());
        return success ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(@AuthenticationPrincipal User user, @PathVariable int id) {
        boolean success = goalDAO.deleteGoal(id, user.getId());
        return success ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}