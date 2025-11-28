package com.example.CashFlowWeb;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    private final AssetDAO assetDAO = new AssetDAO();

    @GetMapping("/assets")
    public List<Asset> getAllAssets(@AuthenticationPrincipal User user) {
        return assetDAO.getAllAssets(user.getId());
    }

    @GetMapping("/assets/{id}")
    public ResponseEntity<Asset> getAssetById(@AuthenticationPrincipal User user, @PathVariable int id) {
        Asset asset = assetDAO.getAssetById(id, user.getId());
        return asset != null ? ResponseEntity.ok(asset) : ResponseEntity.notFound().build();
    }

    @PostMapping("/assets")
    public ResponseEntity<Boolean> addAsset(@AuthenticationPrincipal User user, @RequestBody Asset asset) {
        boolean isSuccess = assetDAO.addAsset(asset, user.getId());
        return isSuccess ? ResponseEntity.ok(true) : ResponseEntity.badRequest().body(false);
    }

    @PutMapping("/assets/{id}")
    public ResponseEntity<Boolean> updateAsset(@AuthenticationPrincipal User user, @PathVariable int id,
            @RequestBody Asset asset) {
        Asset assetToUpdate = new Asset(id, asset.getName(), asset.getTickerSymbol(), asset.getQuantity(),
                asset.getPurchasePrice(), asset.getCurrentPrice(), asset.getAssetType());
        boolean isSuccess = assetDAO.updateAsset(assetToUpdate, user.getId());
        return isSuccess ? ResponseEntity.ok(true) : ResponseEntity.badRequest().body(false);
    }

    @DeleteMapping("/assets/{id}")
    public ResponseEntity<Boolean> deleteAsset(@AuthenticationPrincipal User user, @PathVariable int id) {
        boolean isSuccess = assetDAO.deleteAsset(id, user.getId());
        return isSuccess ? ResponseEntity.ok(true) : ResponseEntity.badRequest().body(false);
    }
}