package com.ecommerce.domain.product.application;

import com.ecommerce.domain.product.dao.CategoryRepository;
import com.ecommerce.domain.product.domain.Category;
import com.ecommerce.domain.product.dto.CategoryResponse;
import com.ecommerce.domain.product.exception.ProductException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> getRootCategories() {
        return categoryRepository.findByParentIsNullAndActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    public List<CategoryResponse> getSubCategories(Long parentId) {
        return categoryRepository.findByParentIdAndActiveTrueOrderBySortOrderAsc(parentId)
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    public CategoryResponse getCategoryWithChildren(Long categoryId) {
        Category category = categoryRepository.findByIdWithChildren(categoryId)
                .orElseThrow(ProductException::categoryNotFound);

        return CategoryResponse.fromWithChildren(category);
    }

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAllActiveOrderByDepthAndSortOrder()
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    public Category findById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(ProductException::categoryNotFound);
    }
}
