// addTransaction メソッドのみ修正が必要です。他は変更ありませんが、念のため全文に近い形で。

    @PostMapping
    public ResponseEntity<Void> addTransaction(@RequestBody Transaction transaction, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userDAO.findByUsername(userDetails.getUsername());
        boolean success = transactionDAO.addTransaction(
                user.getId(),
                transaction.getDate(), 
                transaction.getAmount(), 
                transaction.getType(),
                transaction.getCategoryId(), 
                transaction.getGoalId(), // ★ここを追加
                transaction.getIsFuture(), 
                transaction.getIsExtraordinary());
        return success ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }
