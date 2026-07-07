package com.example.template.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PageResponse<T> {

    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;

    /**
     * Factory method untuk membuat PageResponse dari hasil query Panache.
     * Menggantikan Spring Data Page<T>.
     */
    public static <T> PageResponse<T> of(List<T> content, int page, int size, long total) {
        int totalPages = size == 0 ? 1 : (int) Math.ceil((double) total / (double) size);
        return PageResponse.<T>builder()
                .content(content)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(total)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1)
                .build();
    }
}
