package com.ecommerce.domain.product.domain;

import com.ecommerce.domain.model.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class ProductTest {

    @Nested
    @DisplayName("상품 상태 변경")
    class StatusChangeTest {

        @Test
        @DisplayName("상품을 활성화한다")
        void activate_Success() {
            // given
            Product product = createProduct();

            // when
            product.activate();

            // then
            assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
            assertThat(product.isPurchasable()).isTrue();
        }

        @Test
        @DisplayName("상품을 비활성화한다")
        void deactivate_Success() {
            // given
            Product product = createProduct();
            product.activate();

            // when
            product.deactivate();

            // then
            assertThat(product.getStatus()).isEqualTo(ProductStatus.INACTIVE);
            assertThat(product.isPurchasable()).isFalse();
        }

        @Test
        @DisplayName("상품을 품절 처리한다")
        void markSoldOut_Success() {
            // given
            Product product = createProduct();
            product.activate();

            // when
            product.markSoldOut();

            // then
            assertThat(product.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);
            assertThat(product.isPurchasable()).isFalse();
        }

        @Test
        @DisplayName("상품을 판매 중단 처리한다")
        void discontinue_Success() {
            // given
            Product product = createProduct();
            product.activate();

            // when
            product.discontinue();

            // then
            assertThat(product.getStatus()).isEqualTo(ProductStatus.DISCONTINUED);
            assertThat(product.isPurchasable()).isFalse();
        }
    }

    @Nested
    @DisplayName("할인율 계산")
    class DiscountRateTest {

        @Test
        @DisplayName("할인율을 정확하게 계산한다")
        void getDiscountRate_Success() {
            // given
            Category category = createCategory();
            Product product = Product.builder()
                    .name("할인 상품")
                    .basePrice(Money.of(10000L))
                    .sellingPrice(Money.of(8000L))
                    .category(category)
                    .build();

            // when
            long discountRate = product.getDiscountRate();

            // then
            assertThat(discountRate).isEqualTo(20L);
        }

        @Test
        @DisplayName("할인이 없으면 할인율은 0이다")
        void getDiscountRate_NoDiscount_ReturnsZero() {
            // given
            Category category = createCategory();
            Product product = Product.builder()
                    .name("정가 상품")
                    .basePrice(Money.of(10000L))
                    .sellingPrice(Money.of(10000L))
                    .category(category)
                    .build();

            // when
            long discountRate = product.getDiscountRate();

            // then
            assertThat(discountRate).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("옵션 관리")
    class OptionManagementTest {

        @Test
        @DisplayName("상품에 옵션을 추가한다")
        void addOption_Success() {
            // given
            Product product = createProduct();
            ProductOption option = ProductOption.builder()
                    .product(product)
                    .name("빨강")
                    .additionalPrice(Money.of(1000L))
                    .build();

            // when
            product.addOption(option);

            // then
            assertThat(product.isHasOption()).isTrue();
            assertThat(product.getOptions()).hasSize(1);
        }

        @Test
        @DisplayName("상품의 마지막 옵션을 삭제하면 hasOption이 false가 된다")
        void removeOption_LastOption_HasOptionBecomesFalse() {
            // given
            Product product = createProduct();
            ProductOption option = ProductOption.builder()
                    .product(product)
                    .name("빨강")
                    .additionalPrice(Money.of(1000L))
                    .build();
            product.addOption(option);

            // when
            product.removeOption(option);

            // then
            assertThat(product.isHasOption()).isFalse();
            assertThat(product.getOptions()).isEmpty();
        }
    }

    @Nested
    @DisplayName("상품 정보 수정")
    class UpdateInfoTest {

        @Test
        @DisplayName("상품 정보를 수정한다")
        void updateInfo_Success() {
            // given
            Product product = createProduct();

            // when
            product.updateInfo(
                    "수정된 상품명",
                    "수정된 설명",
                    Money.of(15000L),
                    Money.of(12000L),
                    "new-thumbnail.jpg"
            );

            // then
            assertThat(product.getName()).isEqualTo("수정된 상품명");
            assertThat(product.getDescription()).isEqualTo("수정된 설명");
            assertThat(product.getBasePrice().getAmount()).isEqualTo(15000L);
            assertThat(product.getSellingPrice().getAmount()).isEqualTo(12000L);
        }
    }

    private Product createProduct() {
        Category category = createCategory();
        return Product.builder()
                .name("테스트 상품")
                .description("상품 설명")
                .basePrice(Money.of(10000L))
                .sellingPrice(Money.of(10000L))
                .category(category)
                .build();
    }

    private Category createCategory() {
        Category category = Category.builder()
                .name("전자제품")
                .sortOrder(1)
                .build();
        ReflectionTestUtils.setField(category, "id", 1L);
        return category;
    }
}
