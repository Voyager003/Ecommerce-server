package com.ecommerce.domain.product.application;

import com.ecommerce.domain.model.Money;
import com.ecommerce.domain.product.dao.ProductRepository;
import com.ecommerce.domain.product.domain.Category;
import com.ecommerce.domain.product.domain.Product;
import com.ecommerce.domain.product.domain.ProductStatus;
import com.ecommerce.domain.product.dto.ProductListResponse;
import com.ecommerce.domain.product.dto.ProductResponse;
import com.ecommerce.domain.product.dto.ProductSearchRequest;
import com.ecommerce.domain.product.exception.ProductException;
import com.ecommerce.global.common.PageResponse;
import com.ecommerce.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Nested
    @DisplayName("상품 단건 조회")
    class GetProductTest {

        @Test
        @DisplayName("구매 가능한 상품을 정상적으로 조회한다")
        void getProduct_Success() {
            // given
            Product product = createActiveProduct(1L, "테스트 상품", 10000L);
            given(productRepository.findByIdWithCategoryAndOptions(1L)).willReturn(Optional.of(product));

            // when
            ProductResponse response = productService.getProduct(1L);

            // then
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("테스트 상품");
            assertThat(response.getSellingPrice()).isEqualTo(10000L);
        }

        @Test
        @DisplayName("존재하지 않는 상품 조회 시 예외가 발생한다")
        void getProduct_NotFound_ThrowsException() {
            // given
            given(productRepository.findByIdWithCategoryAndOptions(anyLong())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productService.getProduct(999L))
                    .isInstanceOf(ProductException.class)
                    .satisfies(e -> {
                        ProductException ex = (ProductException) e;
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
                    });
        }

        @Test
        @DisplayName("구매 불가능한 상품 조회 시 예외가 발생한다")
        void getProduct_NotAvailable_ThrowsException() {
            // given
            Product product = createInactiveProduct(1L, "품절 상품");
            given(productRepository.findByIdWithCategoryAndOptions(1L)).willReturn(Optional.of(product));

            // when & then
            assertThatThrownBy(() -> productService.getProduct(1L))
                    .isInstanceOf(ProductException.class)
                    .satisfies(e -> {
                        ProductException ex = (ProductException) e;
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_NOT_AVAILABLE);
                    });
        }
    }

    @Nested
    @DisplayName("상품 목록 조회")
    class GetProductsTest {

        @Test
        @DisplayName("전체 상품 목록을 조회한다")
        void getProducts_All_Success() {
            // given
            Product product1 = createActiveProduct(1L, "상품1", 10000L);
            Product product2 = createActiveProduct(2L, "상품2", 20000L);
            Page<Product> productPage = new PageImpl<>(
                    List.of(product1, product2),
                    PageRequest.of(0, 20),
                    2
            );

            given(productRepository.findByStatus(any(ProductStatus.class), any(PageRequest.class)))
                    .willReturn(productPage);

            ProductSearchRequest request = new ProductSearchRequest();
            request.setPage(0);
            request.setSize(20);

            // when
            PageResponse<ProductListResponse> response = productService.getProducts(request);

            // then
            assertThat(response.getContent()).hasSize(2);
            assertThat(response.getTotalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("키워드로 상품을 검색한다")
        void getProducts_ByKeyword_Success() {
            // given
            Product product = createActiveProduct(1L, "노트북", 1500000L);
            Page<Product> productPage = new PageImpl<>(
                    List.of(product),
                    PageRequest.of(0, 20),
                    1
            );

            given(productRepository.searchByKeyword(any(), any(ProductStatus.class), any(PageRequest.class)))
                    .willReturn(productPage);

            ProductSearchRequest request = new ProductSearchRequest();
            request.setKeyword("노트북");
            request.setPage(0);
            request.setSize(20);

            // when
            PageResponse<ProductListResponse> response = productService.getProducts(request);

            // then
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getName()).isEqualTo("노트북");
        }

        @Test
        @DisplayName("카테고리별 상품을 조회한다")
        void getProducts_ByCategory_Success() {
            // given
            Product product = createActiveProduct(1L, "의류", 50000L);
            Page<Product> productPage = new PageImpl<>(
                    List.of(product),
                    PageRequest.of(0, 20),
                    1
            );

            given(productRepository.findByCategoryIdAndStatus(anyLong(), any(ProductStatus.class), any(PageRequest.class)))
                    .willReturn(productPage);

            ProductSearchRequest request = new ProductSearchRequest();
            request.setCategoryId(1L);
            request.setPage(0);
            request.setSize(20);

            // when
            PageResponse<ProductListResponse> response = productService.getProducts(request);

            // then
            assertThat(response.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("신상품 조회")
    class GetNewArrivalsTest {

        @Test
        @DisplayName("최신 상품 10개를 조회한다")
        void getNewArrivals_Success() {
            // given
            List<Product> products = List.of(
                    createActiveProduct(1L, "신상품1", 10000L),
                    createActiveProduct(2L, "신상품2", 20000L)
            );
            given(productRepository.findTop10ByStatusOrderByCreatedAtDesc(ProductStatus.ACTIVE))
                    .willReturn(products);

            // when
            List<ProductListResponse> response = productService.getNewArrivals();

            // then
            assertThat(response).hasSize(2);
        }
    }

    private Product createActiveProduct(Long id, String name, long price) {
        Category category = createCategory(1L, "전자제품");
        Product product = Product.builder()
                .name(name)
                .description("상품 설명")
                .basePrice(Money.of(price))
                .sellingPrice(Money.of(price))
                .category(category)
                .build();
        ReflectionTestUtils.setField(product, "id", id);
        ReflectionTestUtils.setField(product, "status", ProductStatus.ACTIVE);
        ReflectionTestUtils.setField(product, "createdAt", LocalDateTime.now());
        return product;
    }

    private Product createInactiveProduct(Long id, String name) {
        Category category = createCategory(1L, "전자제품");
        Product product = Product.builder()
                .name(name)
                .description("상품 설명")
                .basePrice(Money.of(10000L))
                .sellingPrice(Money.of(10000L))
                .category(category)
                .build();
        ReflectionTestUtils.setField(product, "id", id);
        ReflectionTestUtils.setField(product, "status", ProductStatus.SOLD_OUT);
        ReflectionTestUtils.setField(product, "createdAt", LocalDateTime.now());
        return product;
    }

    private Category createCategory(Long id, String name) {
        Category category = Category.builder()
                .name(name)
                .sortOrder(1)
                .build();
        ReflectionTestUtils.setField(category, "id", id);
        return category;
    }
}
