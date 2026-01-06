package com.ecommerce.global.common;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Getter
@Setter
public class PageRequest {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private int page = 0;
    private int size = DEFAULT_SIZE;
    private String sort;
    private String direction = "desc";

    public Pageable toPageable() {
        int validSize = Math.min(size, MAX_SIZE);
        if (sort == null || sort.isBlank()) {
            return org.springframework.data.domain.PageRequest.of(page, validSize);
        }
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return org.springframework.data.domain.PageRequest.of(page, validSize, Sort.by(sortDirection, sort));
    }
}
