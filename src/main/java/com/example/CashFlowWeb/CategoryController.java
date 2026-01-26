package com.example.CashFlowWeb;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final UserDAO userDAO = new UserDAO();

    /**
     * すべてのカテゴリのリストを取得します。
     */
    @GetMapping
    public List<Category> getAllCategories(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userDAO.findByUsername(userDetails.getUsername());
        // ★ userId を渡す (DAO内で初回アクセス時にデフォルトカテゴリを作成します)
        return categoryDAO.getAllCategories(user.getId());
    }

    /**
     * 新しいカテゴリを追加します。
     */
    @PostMapping
    public ResponseEntity<Boolean> addCategory(@RequestBody Category category,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userDAO.findByUsername(userDetails.getUsername());

        // ★ userId を渡す
        boolean isSuccess = categoryDAO.addCategory(category.getName(), category.getType(), user.getId());
        return isSuccess ? ResponseEntity.ok(true) : ResponseEntity.badRequest().body(false);
    }

    /**
     * 既存のカテゴリを更新します。
     */
    @PutMapping("/{id}")
    public ResponseEntity<Boolean> updateCategory(@PathVariable int id, @RequestBody Category category,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userDAO.findByUsername(userDetails.getUsername());

        // ★ userId を渡す
        boolean isSuccess = categoryDAO.updateCategory(id, category.getName(), category.getType(), user.getId());
        return isSuccess ? ResponseEntity.ok(true) : ResponseEntity.badRequest().body(false);
    }

    /**
     * カテゴリを削除します。
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteCategory(@PathVariable int id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userDAO.findByUsername(userDetails.getUsername());

        // ★ userId を渡す
        boolean isSuccess = categoryDAO.deleteCategory(id, user.getId());

        if (isSuccess) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.badRequest().body(false); // 使用中の場合など
        }
    }
}
