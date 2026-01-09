package com.ecommerce.global.common;

import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;

import java.util.List;

@Getter
public class PageResponse<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final boolean hasNext;
    private final Long totalElements;
    private final Integer totalPages;

    private PageResponse(List<T> content, int page, int size, boolean hasNext,
                         Long totalElements, Integer totalPages) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.hasNext = hasNext;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.hasNext(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    public static <T> PageResponse<T> from(Slice<T> slice) {
        return new PageResponse<>(
                slice.getContent(),
                slice.getNumber(),
                slice.getSize(),
                slice.hasNext(),
                null,
                null
        );
    }

    public static <T> PageResponse<T> of(List<T> content, int page, int size,
                                          long totalElements, int totalPages) {
        return new PageResponse<>(
                content,
                page,
                size,
                page < totalPages - 1,
                totalElements,
                totalPages
        );
    }
}
