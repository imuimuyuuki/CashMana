package com.example.CashFlowWeb;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

// 注意: ここで import com.example.CashFlowWeb.User; は同じパッケージなので不要ですが、
// org.springframework.security.core.userdetails.User がインポートされないように注意してください。

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalDAO goalDAO = new GoalDAO();

    /**
     * ログインユーザーの目標リストを取得
     */
    @GetMapping
    public List<Goal> getAllGoals(@AuthenticationPrincipal User user) {
        // user.getId() でログイン中のユーザーIDをDAOに渡す
        return goalDAO.getAllGoals(user.getId());
    }

    /**
     * 目標を追加
     */
    @PostMapping
    public ResponseEntity<Void> addGoal(@AuthenticationPrincipal User user, @RequestBody Goal goal) {
        // ユーザーIDを渡して保存
        boolean success = goalDAO.addGoal(goal, user.getId());
        return success ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    /**
     * 目標を更新
     */
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateGoal(@AuthenticationPrincipal User user, @PathVariable int id,
            @RequestBody Goal goal) {
        goal.setId(id);
        // ユーザーIDを渡して、他人のデータを書き換えないようにする
        boolean success = goalDAO.updateGoal(goal, user.getId());
        return success ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    /**
     * 目標を削除
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(@AuthenticationPrincipal User user, @PathVariable int id) {
        // ユーザーIDを渡して、自分のデータだけ削除できるようにする
        boolean success = goalDAO.deleteGoal(id, user.getId());
        return success ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
