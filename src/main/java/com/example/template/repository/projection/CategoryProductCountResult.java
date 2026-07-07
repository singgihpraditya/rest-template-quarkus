package com.example.template.repository.projection;

/**
 * Concrete implementation dari CategoryProductCountProjection.
 * Digunakan sebagai target mapping hasil native query Hibernate.
 */
public class CategoryProductCountResult implements CategoryProductCountProjection {

    private final Long id;
    private final String name;
    private final String description;
    private final Long productCount;

    public CategoryProductCountResult(Long id, String name, String description, Long productCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.productCount = productCount;
    }

    @Override public Long getId() { return id; }
    @Override public String getName() { return name; }
    @Override public String getDescription() { return description; }
    @Override public Long getProductCount() { return productCount; }
}
