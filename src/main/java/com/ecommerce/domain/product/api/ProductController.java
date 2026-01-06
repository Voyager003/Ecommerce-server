package com.ecommerce.domain.product.api;

import com.ecommerce.domain.product.application.ProductService;
import com.ecommerce.domain.product.dto.ProductListResponse;
import com.ecommerce.domain.product.dto.ProductResponse;
import com.ecommerce.domain.product.dto.ProductSearchRequest;
import com.ecommerce.global.common.ApiResponse;
import com.ecommerce.global.common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductListResponse>>> getProducts(
            @ModelAttribute ProductSearchRequest request) {
        PageResponse<ProductListResponse> response = productService.getProducts(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable Long productId) {
        ProductResponse response = productService.getProduct(productId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/new-arrivals")
    public ResponseEntity<ApiResponse<List<ProductListResponse>>> getNewArrivals() {
        List<ProductListResponse> response = productService.getNewArrivals();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<ProductListResponse>>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        ProductSearchRequest request = new ProductSearchRequest();
        request.setKeyword(keyword);
        request.setPage(page);
        request.setSize(size);

        PageResponse<ProductListResponse> response = productService.getProducts(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<PageResponse<ProductListResponse>>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        ProductSearchRequest request = new ProductSearchRequest();
        request.setCategoryId(categoryId);
        request.setPage(page);
        request.setSize(size);

        PageResponse<ProductListResponse> response = productService.getProducts(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
