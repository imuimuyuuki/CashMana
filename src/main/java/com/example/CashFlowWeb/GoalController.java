package com.example.CashFlowWeb;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalDAO goalDAO = new GoalDAO();
    private final UserDAO userDAO = new UserDAO(); // ユーザー特定用

    @GetMapping
    public List<Goal> getAllGoals(@AuthenticationPrincipal UserDetails userDetails) {
        // ログイン中のユーザーIDを使って目標を取得
        User user = userDAO.findByUsername(userDetails.getUsername());
        return goalDAO.getAllGoals(user.getId());
    }

    @PostMapping
    public ResponseEntity<Void> addGoal(@RequestBody Goal goal, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userDAO.findByUsername(userDetails.getUsername());
        // ユーザーIDを渡して保存
        boolean success = goalDAO.addGoal(goal, user.getId());
        return success ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateGoal(@PathVariable int id, @RequestBody Goal goal,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userDAO.findByUsername(userDetails.getUsername());

        goal.setId(id);
        boolean success = goalDAO.updateGoal(goal, user.getId());
        return success ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(@PathVariable int id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userDAO.findByUsername(userDetails.getUsername());

        boolean success = goalDAO.deleteGoal(id, user.getId());
        return success ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
