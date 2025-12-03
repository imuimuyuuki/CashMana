package com.example.CashFlowWeb;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * カテゴリ情報を管理するためのAPIコントローラー。
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryDAO categoryDAO = new CategoryDAO();

    /**
     * すべてのカテゴリのリストを取得します。
     */
    @GetMapping
    public List<Category> getAllCategories() {
        return categoryDAO.getAllCategories();
    }

    /**
     * 新しいカテゴリを追加します。
     */
    @PostMapping
    public ResponseEntity<Boolean> addCategory(@RequestBody Category category) {
        boolean isSuccess = categoryDAO.addCategory(category.getName(), category.getType());
        return isSuccess ? ResponseEntity.ok(true) : ResponseEntity.badRequest().body(false);
    }

    /**
     * 既存のカテゴリを更新します。
     */
    @PutMapping("/{id}")
    public ResponseEntity<Boolean> updateCategory(@PathVariable int id, @RequestBody Category category) {
        boolean isSuccess = categoryDAO.updateCategory(id, category.getName(), category.getType());
        return isSuccess ? ResponseEntity.ok(true) : ResponseEntity.badRequest().body(false);
    }

    /**
     * カテゴリを削除します。
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteCategory(@PathVariable int id) {
        boolean isSuccess = categoryDAO.deleteCategory(id);
        // 削除できない場合(関連する取引がある場合)はDAOがfalseを返す
        if (isSuccess) {
            return ResponseEntity.ok(true);
        } else {
            // クライアント側でエラーメッセージをハンドリングしやすいように、409 Conflictを返す
            return ResponseEntity.status(409).body(false); 
        }
    }
}