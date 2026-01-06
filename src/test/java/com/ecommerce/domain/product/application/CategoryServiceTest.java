package com.ecommerce.domain.product.application;

import com.ecommerce.domain.product.dao.CategoryRepository;
import com.ecommerce.domain.product.domain.Category;
import com.ecommerce.domain.product.dto.CategoryResponse;
import com.ecommerce.domain.product.exception.ProductException;
import com.ecommerce.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private CategoryRepository categoryRepository;

    @Nested
    @DisplayName("루트 카테고리 조회")
    class GetRootCategoriesTest {

        @Test
        @DisplayName("최상위 카테고리 목록을 조회한다")
        void getRootCategories_Success() {
            // given
            Category category1 = createCategory(1L, "전자제품", null);
            Category category2 = createCategory(2L, "의류", null);

            given(categoryRepository.findByParentIsNullAndActiveTrueOrderBySortOrderAsc())
                    .willReturn(List.of(category1, category2));

            // when
            List<CategoryResponse> responses = categoryService.getRootCategories();

            // then
            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getName()).isEqualTo("전자제품");
            assertThat(responses.get(0).getParentId()).isNull();
        }
    }

    @Nested
    @DisplayName("하위 카테고리 조회")
    class GetSubCategoriesTest {

        @Test
        @DisplayName("특정 카테고리의 하위 카테고리를 조회한다")
        void getSubCategories_Success() {
            // given
            Category parent = createCategory(1L, "전자제품", null);
            Category child1 = createCategory(2L, "노트북", parent);
            Category child2 = createCategory(3L, "스마트폰", parent);

            given(categoryRepository.findByParentIdAndActiveTrueOrderBySortOrderAsc(1L))
                    .willReturn(List.of(child1, child2));

            // when
            List<CategoryResponse> responses = categoryService.getSubCategories(1L);

            // then
            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getParentId()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("카테고리 단건 조회")
    class GetCategoryWithChildrenTest {

        @Test
        @DisplayName("카테고리와 하위 카테고리를 함께 조회한다")
        void getCategoryWithChildren_Success() {
            // given
            Category category = createCategory(1L, "전자제품", null);

            given(categoryRepository.findByIdWithChildren(1L)).willReturn(Optional.of(category));

            // when
            CategoryResponse response = categoryService.getCategoryWithChildren(1L);

            // then
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("전자제품");
        }

        @Test
        @DisplayName("존재하지 않는 카테고리 조회 시 예외가 발생한다")
        void getCategoryWithChildren_NotFound_ThrowsException() {
            // given
            given(categoryRepository.findByIdWithChildren(anyLong())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> categoryService.getCategoryWithChildren(999L))
                    .isInstanceOf(ProductException.class)
                    .satisfies(e -> {
                        ProductException ex = (ProductException) e;
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);
                    });
        }
    }

    @Nested
    @DisplayName("전체 카테고리 조회")
    class GetAllCategoriesTest {

        @Test
        @DisplayName("모든 활성 카테고리를 조회한다")
        void getAllCategories_Success() {
            // given
            Category category1 = createCategory(1L, "전자제품", null);
            Category category2 = createCategory(2L, "노트북", category1);
            Category category3 = createCategory(3L, "의류", null);

            given(categoryRepository.findAllActiveOrderByDepthAndSortOrder())
                    .willReturn(List.of(category1, category3, category2));

            // when
            List<CategoryResponse> responses = categoryService.getAllCategories();

            // then
            assertThat(responses).hasSize(3);
        }
    }

    private Category createCategory(Long id, String name, Category parent) {
        Category category = Category.builder()
                .name(name)
                .parent(parent)
                .sortOrder(1)
                .build();
        ReflectionTestUtils.setField(category, "id", id);
        return category;
    }
}
