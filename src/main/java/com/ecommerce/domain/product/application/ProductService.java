package com.ecommerce.domain.product.application;

import com.ecommerce.domain.product.dao.ProductRepository;
import com.ecommerce.domain.product.domain.Product;
import com.ecommerce.domain.product.domain.ProductStatus;
import com.ecommerce.domain.product.dto.ProductListResponse;
import com.ecommerce.domain.product.dto.ProductResponse;
import com.ecommerce.domain.product.dto.ProductSearchRequest;
import com.ecommerce.domain.product.exception.ProductException;
import com.ecommerce.global.common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public ProductResponse getProduct(Long productId) {
        Product product = productRepository.findByIdWithCategoryAndOptions(productId)
                .orElseThrow(ProductException::notFound);

        if (!product.isPurchasable()) {
            throw ProductException.notAvailable();
        }

        return ProductResponse.fromWithOptions(product);
    }

    public ProductResponse getProductDetail(Long productId) {
        Product product = productRepository.findByIdWithCategoryAndOptions(productId)
                .orElseThrow(ProductException::notFound);

        return ProductResponse.fromWithOptions(product);
    }

    public PageResponse<ProductListResponse> getProducts(ProductSearchRequest request) {
        PageRequest pageRequest = createPageRequest(request);

        Page<Product> productPage;

        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            productPage = productRepository.searchByKeyword(
                    request.getKeyword(),
                    ProductStatus.ACTIVE,
                    pageRequest
            );
        } else if (request.getCategoryId() != null) {
            productPage = productRepository.findByCategoryIdAndStatus(
                    request.getCategoryId(),
                    ProductStatus.ACTIVE,
                    pageRequest
            );
        } else {
            productPage = productRepository.findByStatus(ProductStatus.ACTIVE, pageRequest);
        }

        List<ProductListResponse> content = productPage.getContent().stream()
                .map(ProductListResponse::from)
                .toList();

        return PageResponse.of(
                content,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages()
        );
    }

    public List<ProductListResponse> getNewArrivals() {
        return productRepository.findTop10ByStatusOrderByCreatedAtDesc(ProductStatus.ACTIVE)
                .stream()
                .map(ProductListResponse::from)
                .toList();
    }

    public Product findById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(ProductException::notFound);
    }

    public Product findByIdWithOptions(Long productId) {
        return productRepository.findByIdWithCategoryAndOptions(productId)
                .orElseThrow(ProductException::notFound);
    }

    private PageRequest createPageRequest(ProductSearchRequest request) {
        Sort.Direction direction = "asc".equalsIgnoreCase(request.getDirection())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(direction, request.getSort())
        );
    }
}
