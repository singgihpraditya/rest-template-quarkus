package com.example.template.repository.projection;

/**
 * Projection interface untuk hasil native query kategori + jumlah produk.
 * Diimplementasikan oleh CategoryProductCountResult (concrete class untuk mapping Hibernate).
 */
public interface CategoryProductCountProjection {
    Long getId();
    String getName();
    String getDescription();
    Long getProductCount();
}
