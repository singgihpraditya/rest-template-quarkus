package com.example.template.service;

import com.example.template.dto.request.ProductRequest;
import com.example.template.dto.response.PageResponse;
import com.example.template.dto.response.ProductResponse;
import com.example.template.entity.Category;
import com.example.template.entity.Product;
import com.example.template.entity.Tag;
import com.example.template.exception.ResourceNotFoundException;
import com.example.template.repository.CategoryRepository;
import com.example.template.repository.ProductRepository;
import com.example.template.repository.TagRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class ProductService {

    @Inject
    ProductRepository productRepository;

    @Inject
    CategoryRepository categoryRepository;

    @Inject
    TagRepository tagRepository;

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Category category = categoryRepository.findByIdOptional(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));

        Set<Tag> tags = resolveTags(request.getTagIds());

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .publishedAt(request.getPublishedAt())
                .category(category)
                .tags(tags)
                .build();

        productRepository.persist(product);
        log.info("Produk baru dibuat: {}", product.getName());
        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse findById(Long id) {
        Product product = productRepository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        return ProductResponse.from(product);
    }

    @Transactional
    public PageResponse<ProductResponse> findAll(int page, int size, String search) {
        long total;
        List<Product> products;

        if (search != null && !search.isBlank()) {
            var query = productRepository.findByNameContainingIgnoreCase(search);
            total = query.count();
            products = query.page(io.quarkus.panache.common.Page.of(page, size)).list();
        } else {
            var query = productRepository.findAll(Sort.by("createdAt").descending());
            total = query.count();
            products = query.page(io.quarkus.panache.common.Page.of(page, size)).list();
        }

        List<ProductResponse> content = products.stream().map(ProductResponse::from).toList();
        return PageResponse.of(content, page, size, total);
    }

    @Transactional
    public PageResponse<ProductResponse> findByCategory(Long categoryId, int page, int size) {
        categoryRepository.findByIdOptional(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));

        var query = productRepository.findByCategoryId(categoryId);
        long total = query.count();
        List<Product> products = query.page(io.quarkus.panache.common.Page.of(page, size)).list();
        List<ProductResponse> content = products.stream().map(ProductResponse::from).toList();
        return PageResponse.of(content, page, size, total);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        Category category = categoryRepository.findByIdOptional(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setPublishedAt(request.getPublishedAt());
        product.setCategory(category);
        product.setTags(resolveTags(request.getTagIds()));

        log.info("Produk diupdate: {}", product.getName());
        return ProductResponse.from(product);
    }

    @Transactional
    public void delete(Long id) {
        Product product = productRepository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        productRepository.delete(product);
        log.info("Produk dihapus: {}", product.getName());
    }

    @Transactional
    public List<ProductResponse> getTopProductsByPrice(int limit) {
        return productRepository.findTopByPrice(limit).stream()
                .map(ProductResponse::from)
                .toList();
    }

    @Transactional
    public List<ProductResponse> findByTag(String tagName) {
        return productRepository.findByTagName(tagName).stream()
                .map(ProductResponse::from)
                .toList();
    }

    private Set<Tag> resolveTags(Set<Long> tagIds) {
        return tagIds.stream()
                .map(tagId -> tagRepository.findByIdOptional(tagId)
                        .orElseThrow(() -> new ResourceNotFoundException("Tag", tagId)))
                .collect(Collectors.toSet());
    }
}
