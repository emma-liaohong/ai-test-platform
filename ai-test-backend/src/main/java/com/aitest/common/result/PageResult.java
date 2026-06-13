package com.aitest.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Paginated response wrapper
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    /** Total record count */
    private Long total;

    /** Current page number */
    private Long current;

    /** Page size */
    private Long size;

    /** Total pages */
    private Long pages;

    /** Data list for current page */
    private List<T> records;

    public static <T> PageResult<T> of(Long total, Long current, Long size, Long pages, List<T> records) {
        return new PageResult<>(total, current, size, pages, records);
    }
}
