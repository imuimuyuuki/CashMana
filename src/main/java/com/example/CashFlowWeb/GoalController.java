package com.example.CashFlowWeb;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalDAO goalDAO = new GoalDAO();

    @GetMapping
    public List<Goal> getAllGoals() {
        return goalDAO.getAllGoals();
    }

    @PostMapping
    public ResponseEntity<Void> addGoal(@RequestBody Goal goal) {
        boolean success = goalDAO.addGoal(goal);
        return success ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateGoal(@PathVariable int id, @RequestBody Goal goal) {
        goal.setId(id);
        boolean success = goalDAO.updateGoal(goal);
        return success ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(@PathVariable int id) {
        boolean success = goalDAO.deleteGoal(id);
        return success ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}