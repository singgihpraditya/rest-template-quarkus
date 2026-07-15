package com.example.template.quarkus.config;

/**
 * Konstanta nama cache — sama persis dengan Spring Boot versi.
 * Pakai konstanta ini di @CacheResult / @CacheInvalidateAll, bukan string literal.
 */
public final class CacheConfig {

    public static final String CATEGORIES_WITH_PRODUCT_COUNT_CACHE = "categories-with-product-count";

    private CacheConfig() {}
}
