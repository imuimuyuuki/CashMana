package com.example.CashFlowWeb;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryDAO categoryDAO = new CategoryDAO();

    @GetMapping
    public List<Category> getAllCategories() {
        return categoryDAO.getAllCategories();
    }

    @PostMapping
    public ResponseEntity<Boolean> addCategory(@RequestBody Category category) {
        boolean isSuccess = categoryDAO.addCategory(category.getName(), category.getType());
        return isSuccess ? ResponseEntity.ok(true) : ResponseEntity.badRequest().body(false);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Boolean> updateCategory(@PathVariable int id, @RequestBody Category category) {
        boolean isSuccess = categoryDAO.updateCategory(id, category.getName(), category.getType());
        return isSuccess ? ResponseEntity.ok(true) : ResponseEntity.badRequest().body(false);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteCategory(@PathVariable int id) {
        boolean isSuccess = categoryDAO.deleteCategory(id);
        return isSuccess ? ResponseEntity.ok(true) : ResponseEntity.badRequest().body(false);
    }
}