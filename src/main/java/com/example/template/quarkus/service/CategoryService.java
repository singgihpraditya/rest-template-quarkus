package com.example.template.quarkus.service;

import com.example.template.quarkus.dto.request.CategoryRequest;
import com.example.template.quarkus.dto.response.CategoryResponse;
import com.example.template.quarkus.dto.response.PageResponse;
import com.example.template.quarkus.entity.Category;
import com.example.template.quarkus.exception.BusinessException;
import com.example.template.quarkus.exception.ResourceNotFoundException;
import com.example.template.quarkus.repository.CategoryRepository;
import com.example.template.quarkus.repository.projection.CategoryProductCountProjection;
import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.example.template.quarkus.config.CacheConfig.CATEGORIES_WITH_PRODUCT_COUNT_CACHE;

@ApplicationScoped
@Slf4j
public class CategoryService {

    @Inject
    CategoryRepository categoryRepository;

    @Transactional
    @CacheInvalidateAll(cacheName = CATEGORIES_WITH_PRODUCT_COUNT_CACHE)
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new BusinessException("Kategori dengan nama '" + request.getName() + "' sudah ada");
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        categoryRepository.persist(category);
        log.info("Kategori baru dibuat: {}", category.getName());
        return CategoryResponse.from(category);
    }

    @Transactional
    public CategoryResponse findById(Long id) {
        Category category = categoryRepository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        return CategoryResponse.from(category);
    }

    @Transactional
    public PageResponse<CategoryResponse> findAll(int page, int size, String search) {
        long total;
        List<Category> categories;

        if (search != null && !search.isBlank()) {
            var query = categoryRepository.findByNameContainingIgnoreCase(search);
            total = query.count();
            categories = query.page(io.quarkus.panache.common.Page.of(page, size)).list();
        } else {
            var query = categoryRepository.findAll(Sort.by("name").ascending());
            total = query.count();
            categories = query.page(io.quarkus.panache.common.Page.of(page, size)).list();
        }

        List<CategoryResponse> content = categories.stream().map(CategoryResponse::from).toList();
        return PageResponse.of(content, page, size, total);
    }

    @Transactional
    @CacheInvalidateAll(cacheName = CATEGORIES_WITH_PRODUCT_COUNT_CACHE)
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = categoryRepository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));

        if (!category.getName().equals(request.getName()) && categoryRepository.existsByName(request.getName())) {
            throw new BusinessException("Kategori dengan nama '" + request.getName() + "' sudah ada");
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        log.info("Kategori diupdate: {}", category.getName());
        return CategoryResponse.from(category);
    }

    @Transactional
    @CacheInvalidateAll(cacheName = CATEGORIES_WITH_PRODUCT_COUNT_CACHE)
    public void delete(Long id) {
        Category category = categoryRepository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));

        if (!category.getProducts().isEmpty()) {
            throw new BusinessException("Kategori tidak bisa dihapus karena masih memiliki produk");
        }

        categoryRepository.delete(category);
        log.info("Kategori dihapus: {}", category.getName());
    }

    /**
     * Native query: jumlah produk per kategori.
     * @CacheResult menggantikan Spring's @Cacheable.
     */
    @Transactional
    @CacheResult(cacheName = CATEGORIES_WITH_PRODUCT_COUNT_CACHE)
    public List<Map<String, Object>> getCategoriesWithProductCount() {
        List<CategoryProductCountProjection> results = categoryRepository.findCategoriesWithProductCount();

        return results.stream().<Map<String, Object>>map(row -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", row.getId());
            map.put("name", row.getName());
            map.put("description", row.getDescription() != null ? row.getDescription() : "");
            map.put("product_count", row.getProductCount());
            return map;
        }).toList();
    }
}
