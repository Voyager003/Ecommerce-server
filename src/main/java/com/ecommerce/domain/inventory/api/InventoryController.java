package com.ecommerce.domain.inventory.api;

import com.ecommerce.domain.inventory.application.InventoryService;
import com.ecommerce.domain.inventory.domain.Inventory;
import com.ecommerce.domain.inventory.domain.InventoryHistory;
import com.ecommerce.domain.inventory.dto.InventoryCreateRequest;
import com.ecommerce.domain.inventory.dto.InventoryHistoryResponse;
import com.ecommerce.domain.inventory.dto.InventoryResponse;
import com.ecommerce.domain.inventory.dto.StockAdjustRequest;
import com.ecommerce.global.common.ApiResponse;
import com.ecommerce.global.common.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<InventoryResponse>> createInventory(
            @Valid @RequestBody InventoryCreateRequest request) {
        Inventory inventory = inventoryService.createInventory(
                request.getProductId(),
                request.getProductOptionId(),
                request.getQuantity());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(InventoryResponse.from(inventory)));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getInventoriesByProduct(
            @PathVariable Long productId) {
        List<Inventory> inventories = inventoryService.getInventoriesByProductId(productId);
        List<InventoryResponse> responses = inventories.stream()
                .map(InventoryResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/product/{productId}/option/{optionId}")
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventory(
            @PathVariable Long productId,
            @PathVariable Long optionId) {
        Inventory inventory = inventoryService.getInventory(productId, optionId);
        return ResponseEntity.ok(ApiResponse.ok(InventoryResponse.from(inventory)));
    }

    @GetMapping("/product/{productId}/default")
    public ResponseEntity<ApiResponse<InventoryResponse>> getDefaultInventory(
            @PathVariable Long productId) {
        Inventory inventory = inventoryService.getInventory(productId, null);
        return ResponseEntity.ok(ApiResponse.ok(InventoryResponse.from(inventory)));
    }

    @PostMapping("/product/{productId}/option/{optionId}/add")
    public ResponseEntity<ApiResponse<Void>> addStock(
            @PathVariable Long productId,
            @PathVariable Long optionId,
            @Valid @RequestBody StockAdjustRequest request) {
        inventoryService.addStock(productId, optionId, request.getQuantity(), request.getReason());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/product/{productId}/default/add")
    public ResponseEntity<ApiResponse<Void>> addStockDefault(
            @PathVariable Long productId,
            @Valid @RequestBody StockAdjustRequest request) {
        inventoryService.addStock(productId, null, request.getQuantity(), request.getReason());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/{inventoryId}/history")
    public ResponseEntity<ApiResponse<PageResponse<InventoryHistoryResponse>>> getInventoryHistory(
            @PathVariable Long inventoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<InventoryHistory> historyPage = inventoryService.getInventoryHistory(
                inventoryId, PageRequest.of(page, size));
        PageResponse<InventoryHistoryResponse> response = PageResponse.from(
                historyPage.map(InventoryHistoryResponse::from));
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/product/{productId}/available")
    public ResponseEntity<ApiResponse<Integer>> getAvailableQuantity(
            @PathVariable Long productId,
            @RequestParam(required = false) Long optionId) {
        int availableQuantity = inventoryService.getAvailableQuantity(productId, optionId);
        return ResponseEntity.ok(ApiResponse.ok(availableQuantity));
    }

    @GetMapping("/product/{productId}/check")
    public ResponseEntity<ApiResponse<Boolean>> hasStock(
            @PathVariable Long productId,
            @RequestParam(required = false) Long optionId,
            @RequestParam int quantity) {
        boolean hasStock = inventoryService.hasStock(productId, optionId, quantity);
        return ResponseEntity.ok(ApiResponse.ok(hasStock));
    }
}
