package com.example.template.quarkus.dto.response;

import com.example.template.quarkus.entity.Product;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Builder
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private int stock;
    private LocalDateTime publishedAt;
    private CategoryResponse category;
    private Set<TagResponse> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProductResponse from(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .publishedAt(product.getPublishedAt())
                .category(CategoryResponse.from(product.getCategory()))
                .tags(product.getTags().stream().map(TagResponse::from).collect(Collectors.toSet()))
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
