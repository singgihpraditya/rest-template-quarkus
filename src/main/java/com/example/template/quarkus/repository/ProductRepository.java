package com.example.template.quarkus.repository;

import com.example.template.quarkus.entity.Product;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;

@ApplicationScoped
public class ProductRepository implements PanacheRepository<Product> {

    @Inject
    EntityManager em;

    public PanacheQuery<Product> findByNameContainingIgnoreCase(String keyword) {
        return find("LOWER(name) LIKE LOWER(?1)", Sort.by("createdAt").descending(), "%" + keyword + "%");
    }

    public PanacheQuery<Product> findByCategoryId(Long categoryId) {
        return find("category.id", Sort.by("createdAt").descending(), categoryId);
    }

    /**
     * Native query: N produk dengan harga tertinggi.
     */
    @SuppressWarnings("unchecked")
    public List<Product> findTopByPrice(int limit) {
        return em.createNativeQuery(
                "SELECT * FROM products ORDER BY price DESC LIMIT :limit", Product.class
        ).setParameter("limit", limit).getResultList();
    }

    /**
     * Native query: produk berdasarkan nama tag (JOIN ke product_tags + tags).
     */
    @SuppressWarnings("unchecked")
    public List<Product> findByTagName(String tagName) {
        return em.createNativeQuery(
                "SELECT p.* FROM products p " +
                "JOIN product_tags pt ON pt.product_id = p.id " +
                "JOIN tags t ON t.id = pt.tag_id " +
                "WHERE LOWER(t.name) = LOWER(:tagName)", Product.class
        ).setParameter("tagName", tagName).getResultList();
    }
}
