package com.example.CashFlowWeb;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    private final AssetDAO assetDAO = new AssetDAO();
    private final UserDAO userDAO = new UserDAO(); // ★ ユーザーID取得用にUserDAOを追加

    @GetMapping("/assets")
    public List<Asset> getAllAssets(@AuthenticationPrincipal UserDetails userDetails) {
        // ログイン中のユーザーを特定
        User user = userDAO.findByUsername(userDetails.getUsername());
        // そのユーザーのIDを渡して資産を取得
        return assetDAO.getAllAssets(user.getId());
    }

    @GetMapping("/assets/{id}")
    public ResponseEntity<Asset> getAssetById(@PathVariable int id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userDAO.findByUsername(userDetails.getUsername());

        Asset asset = assetDAO.getAssetById(id, user.getId()); // ★ userIdを追加
        if (asset != null) {
            return ResponseEntity.ok(asset);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/assets")
    public ResponseEntity<Boolean> addAsset(@RequestBody Asset asset,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userDAO.findByUsername(userDetails.getUsername());

        // ★ userIdを渡して保存
        boolean isSuccess = assetDAO.addAsset(asset, user.getId());
        return isSuccess ? ResponseEntity.ok(true) : ResponseEntity.badRequest().body(false);
    }

    @PutMapping("/assets/{id}")
    public ResponseEntity<Boolean> updateAsset(@PathVariable int id, @RequestBody Asset asset,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userDAO.findByUsername(userDetails.getUsername());

        Asset assetToUpdate = new Asset(id, asset.getName(), asset.getTickerSymbol(), asset.getQuantity(),
                asset.getPurchasePrice(), asset.getCurrentPrice(), asset.getAssetType());

        // ★ userIdを渡して、自分のデータのみ更新可能にする
        boolean isSuccess = assetDAO.updateAsset(assetToUpdate, user.getId());
        return isSuccess ? ResponseEntity.ok(true) : ResponseEntity.badRequest().body(false);
    }

    @DeleteMapping("/assets/{id}")
    public ResponseEntity<Boolean> deleteAsset(@PathVariable int id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userDAO.findByUsername(userDetails.getUsername());

        // ★ userIdを渡して、自分のデータのみ削除可能にする
        boolean isSuccess = assetDAO.deleteAsset(id, user.getId());
        return isSuccess ? ResponseEntity.ok(true) : ResponseEntity.badRequest().body(false);
    }
}
