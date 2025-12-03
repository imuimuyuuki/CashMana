package com.example.CashFlowWeb;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {
    
    private final AssetDAO assetDAO = new AssetDAO();

    @GetMapping("/assets")
    public List<Asset> getAllAssets() {
        return assetDAO.getAllAssets();
    }

    @GetMapping("/assets/{id}")
    public ResponseEntity<Asset> getAssetById(@PathVariable int id) {
        Asset asset = assetDAO.getAssetById(id);
        if (asset != null) {
            return ResponseEntity.ok(asset);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/assets")
    public ResponseEntity<Boolean> addAsset(@RequestBody Asset asset) {
        boolean isSuccess = assetDAO.addAsset(asset);
        return isSuccess ? ResponseEntity.ok(true) : ResponseEntity.badRequest().body(false);
    }

    @PutMapping("/assets/{id}")
    public ResponseEntity<Boolean> updateAsset(@PathVariable int id, @RequestBody Asset asset) {
        Asset assetToUpdate = new Asset(id, asset.getName(), asset.getTickerSymbol(), asset.getQuantity(), asset.getPurchasePrice(), asset.getCurrentPrice(), asset.getAssetType());
        boolean isSuccess = assetDAO.updateAsset(assetToUpdate);
        return isSuccess ? ResponseEntity.ok(true) : ResponseEntity.badRequest().body(false);
    }

    @DeleteMapping("/assets/{id}")
    public ResponseEntity<Boolean> deleteAsset(@PathVariable int id) {
        boolean isSuccess = assetDAO.deleteAsset(id);
        return isSuccess ? ResponseEntity.ok(true) : ResponseEntity.badRequest().body(false);
    }
}